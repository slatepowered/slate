package com.orbyfied.slate.bootstrap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Provides information about the environment a process spawned by a slate cluster is alive in
 */
@RequiredArgsConstructor
public final class ApplicationBootstrapEnvironment {

  /* The standard names of bootstrap environment variables */
  public static final String ENV_DEPLOYED = "slate.deployed";
  public static final String ENV_DEBUG = "slate.debug";
  public static final String ENV_CLUSTER_SHARED_DIR = "slate.cluster.shared";
  public static final String ENV_BUNDLED_LIB_CACHE = "slate.boot.bundled-lib-cache";
  public static final String ENV_PROVIDED_MODULES = "slate.boot.provided";

  private static ApplicationBootstrapEnvironment current;

  public static ApplicationBootstrapEnvironment current() {
    if (current == null) {
      boolean deployed = loadEnvOr(ENV_DEPLOYED, Boolean::parseBoolean, false);
      boolean debug = loadEnvOr(ENV_DEBUG, Boolean::parseBoolean, false);
      Path clusterSharedDirectory = loadEnvOr(ENV_CLUSTER_SHARED_DIR, Path::of, Path.of(System.getProperty("user.home"), "testing/.slate.cluster", "shared"));
      Path bundledLibraryCache = loadEnvOr(ENV_BUNDLED_LIB_CACHE, Path::of, Path.of(System.getProperty("user.home"), "testing/.slate.cluster", "cache", "bundled-libs"));
      List<URL> providedModules = loadEnvOr(ENV_PROVIDED_MODULES, str -> Arrays.stream(str.split(";")).map(ApplicationBootstrapEnvironment::newURL).toList(), List.of());

      current = new ApplicationBootstrapEnvironment(deployed, debug, clusterSharedDirectory, bundledLibraryCache, providedModules);
    }

    return current;
  }

  /// Whether this process was created by a slate cluster
  private final @Getter boolean deployed;
  /// Whether the debug flag should be enabled
  private final @Getter boolean debug;
  /// The path to the common shared directory including the package repository
  private final @Getter Path clusterSharedDirectory;
  /// The path to the shared bundled library cache
  private final @Getter Path bundledLibraryCache;
  /// The modules, provided by the spawning process, to be loaded to the bootstrap loader if applicable
  private final @Getter List<URL> providedModules;

  /* Utilities */

  private static <T> T loadEnvOr(String vName, Function<String, T> function, T def) {
    String s = System.getProperty(vName);
    if (s == null) {
      return def;
    }

    return function.apply(s);
  }

  @SneakyThrows
  private static URL newURL(String s) {
    return new URL(s);
  }

}
