package com.orbyfied.slate.bootstrap;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.orbyfied.slate.util.functional.ThrowingSupplier;
import com.orbyfied.slate.util.loader.PriorityURLClassLoader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Common boostrap for all standalone applications and such. Responsible for loading
 * bundled libraries which must be loaded early, usually to facilitate the dynamic
 * package downloading process for further bootstrapping.
 */
public final class ApplicationBootstrap {

  /* Common Build Constants */
  public static final String ROOT_MODULE_INFO_RESOURCE = "META-INF/slate-module-metadata/component.json";
  public static final String BUNDLED_LIBRARIES_RESOURCE = "META-INF/bundled-libs/";

  static boolean DEBUG = true; // initial value, updated by `env` values after load
  static ApplicationBootstrapEnvironment env;

  // Publicly exported current app info
  static ApplicationInfo applicationInfo;
  static PriorityURLClassLoader applicationLoader;
  static BootstrapInfo bootstrapInfo;

  public static void main(String[] args) throws Throwable {
    log("Bootstrapping slate module, loading env");
    env = ApplicationBootstrapEnvironment.current();
    DEBUG = DEBUG || env.isDebug();
    log("Loaded env: debug(" + DEBUG + ") deployed(" + env.isDeployed() + ") bundledLibCache(" + env.getBundledLibraryRepository() + ") provided(" + env.getProvidedModules().size() + ")");

    // create info object to be populated
    ApplicationInfo appInfo = new ApplicationInfo();

    // find and open application jar
    final CodeSource codeSource = ApplicationBootstrap.class.getProtectionDomain()
        .getCodeSource();
    final Path selfJarPath = Path.of(codeSource.getLocation().toURI());
    log("Found self .jar path: " + selfJarPath);
    if (Files.isDirectory(selfJarPath)) {
      throw new SlateBootstrapException("Expected self .jar path (" + selfJarPath + ") is a directory");
    }

    final Checklist checklist = new Checklist();
    try (JarFile jar = new JarFile(selfJarPath.toFile())) {
      final Path embeddedLibRepo = env.getBundledLibraryRepository();
      log("Embedded library repo: " + embeddedLibRepo);

      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();

        // check for root module metadata
        if (name.equals(ROOT_MODULE_INFO_RESOURCE)) {
          log("Parsing root module metadata resource `" + name + "`");

          try (InputStream is = jar.getInputStream(entry)) {
            // parse application info json
            JsonObject metadata = Json.parse(new BufferedReader(new InputStreamReader(is))).asObject();
            appInfo.loadRootMetadata(metadata);
          }

          checklist.completed(ROOT_MODULE_INFO_RESOURCE);
          log("Completed parse of root module info, identified name(" + appInfo.name + ") component(" + appInfo.component + ") version(" + appInfo.version + ")");
          log("    Package Q: " + appInfo.packageQualifier + ", Component Q: " + appInfo.componentQualifier);
          log("    Qualifier hash: " + appInfo.qualifierHash + " -- " + appInfo.qualifierHash.substring(0, 20));
        }

        // extractable bundled library files
        if (name.startsWith(BUNDLED_LIBRARIES_RESOURCE)) {
          String filename = Path.of(name).getFileName().toString();

          // check for pre-bundled library (ends with .properties)
          if (filename.endsWith(".properties")) {
            String jarFilename = filename.replace(".properties", ".jar");
            log("L: Bundled .properties library manifest " + filename + " at resource " + name);

            // parse properties
            Properties properties = new Properties();
            try (InputStream is = jar.getInputStream(entry)) {
              properties.load(is);
            }

            // find provider and register
            String providerProperty = properties.getProperty("provider");
            if (providerProperty.equals("provided")) {
              Path output = embeddedLibRepo.resolve(jarFilename);
              appInfo.providedLibraries.add(output.toUri().toURL());
              if (!Files.exists(output)) {
                throw new SlateBootstrapException("Bundled library manifest '" + filename + "' was expected to be provided, but file(" + output + ") does not exist");
              }

              continue;
            }

            throw new SlateBootstrapException("Invalid provider property for bundled library manifest '" + filename + "': " + providerProperty);
          }

          Path output = embeddedLibRepo.resolve(filename);
          log("L: Bundled .jar library " + filename + " at resource " + name);
          appInfo.providedLibraries.add(output.toUri().toURL());

          // extract the file if it hasnt been done yet
          if (Files.exists(output)) {
            log("L:  Found library cached at path " + output);
            continue;
          }

          try (InputStream is = jar.getInputStream(entry)) {
            Files.copy(is, output);
          }

          log("L:  Extracted library to path " + output);
        }
      }

      log("Completed jar entry enumeration, closed jar file");

      // check integrity
      checklist.completionOrThrow(ROOT_MODULE_INFO_RESOURCE, () ->
          new SlateBootstrapException("Could not find root module metadata resource '" + ROOT_MODULE_INFO_RESOURCE + "' in jar: " + selfJarPath));
      if (appInfo.entrypoint == null)
        throw new SlateBootstrapException("Invalid application module metadata, no entry point property");
      applicationInfo = appInfo;

      log("Added " + env.getProvidedModules().size() + " provided modules to bootstrap classpath");
      appInfo.providedLibraries.addAll(env.getProvidedModules());

      log("Creating application class loader with " + appInfo.providedLibraries.size() + " initial libraries");
      URL[] urls = appInfo.providedLibraries.toArray(new URL[0]);
      applicationLoader = new PriorityURLClassLoader(urls, ApplicationBootstrap.class.getClassLoader());

      log("Resolving and instantiating entrypoint class: " + appInfo.entrypoint);
      Class<?> entryClass = doOrRewriteError(() -> Class.forName(appInfo.entrypoint, true, applicationLoader),
          th -> new SlateBootstrapException("Could not find or failed to load entry class by name " + appInfo.entrypoint, th));
      Constructor<?> constructor = doOrRewriteError(() -> entryClass.getConstructor(),
          th -> new SlateBootstrapException("Could not find a compatible constructor for entry class " + appInfo.entrypoint, th));
      Object instance = appInfo.entryInstance = doOrRewriteError(() -> constructor.newInstance(),
          th -> new SlateBootstrapException("Failed to instantiate entry class using " + constructor, th));

      log("Resolving `void start(...)` entrypoint method");
      Method method = doOrRewriteError(() -> entryClass.getDeclaredMethod("start"),
          th -> new SlateBootstrapException("Could not find a compatible start(...) method in entry class " + appInfo.entrypoint, th));
      method.setAccessible(true);

      try {
        log("Invoking application entrypoint");
        method.invoke(instance);
      } catch (InvocationTargetException ex) {
        throw ex.getCause();
      }
    }
  }

  public static ApplicationInfo application() {
    return applicationInfo;
  }

  public static PriorityURLClassLoader classLoader() {
    return applicationLoader;
  }

  public static BootstrapInfo bootstrapInfo() {
    return bootstrapInfo;
  }

  private static void log(Object ob) {
    if (DEBUG) System.err.println("[slate::bootstrap] " + ob);
  }

  private static <T> T doOrRewriteError(ThrowingSupplier<T> supplier, Function<Throwable, ? extends RuntimeException> function) {
    try {
      return supplier.get();
    } catch (Throwable thr) {
      throw function.apply(thr);
    }
  }

}
