package slatepowered.slate.packages.key;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.ResolvedPackage;
import slatepowered.slate.packages.local.LocalJavaPackage;
import slatepowered.veru.io.FileUtil;
import slatepowered.veru.misc.Throwables;
import slatepowered.veru.runtime.JavaVersion;
import slatepowered.veru.string.StringReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a JDK/JRE package.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class JavaPackageKey extends ResolvedPackage<JavaPackageKey, LocalJavaPackage> implements TrivialPackageKey<LocalJavaPackage> {

    /**
     * Create a package key for specifically JDK installations of the given version.
     *
     * @param version The version.
     * @return The key.
     */
    public static JavaPackageKey jdk(JavaVersion version) {
        return new JavaPackageKey(Type.JDK, version, null);
    }

    /**
     * Create a package key for specifically JRE installations of the given version.
     *
     * @param version The version.
     * @return The key.
     */
    public static JavaPackageKey jre(JavaVersion version) {
        return new JavaPackageKey(Type.JRE, version, null);
    }

    /**
     * Create a package key for any Java installations of the given version.
     *
     * @param version The version.
     * @return The key.
     */
    public static JavaPackageKey any(JavaVersion version) {
        return new JavaPackageKey(Type.ANY, version, null);
    }

    public enum Type {
        // Only accept JDK installations
        JDK,
        // Only accept JRE installations
        JRE,
        // Accept both JDK and JRE installations
        ANY
    }

    /**
     * The installation type.
     */
    private Type type;

    /**
     * The Java version required.
     */
    private JavaVersion version;

    // The cached package UUID
    private transient UUID uuid;

    @Override
    public UUID toUUID() {
        if (uuid == null) {
            uuid = PackageKey.stringToUUID("javapkg" + type + "." + version.getMajor() + "." + version.getMinor());
        }

        return uuid;
    }

    @Override
    public String getProvider() {
        return "java";
    }

    @Override
    public CompletableFuture<ResolvedPackage<?, LocalJavaPackage>> resolve(PackageManager manager) {
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public JavaPackageKey getKey() {
        return this;
    }

    // add the path object representation of the given string
    // if it parses successfully and the file/dir exists
    private void addPathIfPresent(List<Path> paths, String pathStr) {
        try {
            Path path = Paths.get(pathStr);
            if (Files.exists(path)) {
                paths.add(path);
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    public CompletableFuture<LocalJavaPackage> installLocally(PackageManager manager, Path path) {
        try {
            // search for local Java installation, then symlink
            // the required files from there to here
            List<Path> javaInstallationDirectories = new ArrayList<>();
            if (System.getenv("JAVA_INSTALLATIONS") != null) {
                javaInstallationDirectories.add(Paths.get(System.getenv("JAVA_INSTALLATIONS")));
            }

            addPathIfPresent(javaInstallationDirectories, "/usr/lib/jvm/");
            addPathIfPresent(javaInstallationDirectories, "C:\\Program Files\\Java");
            // * no mac support L idk where to find java there
            // * tbf who's gonna run a server on macOS

            for (Path installDir : javaInstallationDirectories) {
                Optional<Path> optionalInstallationPath = Files
                        .list(installDir)
                        .filter(Files::isDirectory)
                        .filter(dir -> {
                            // check for bin/java
                            if (!Files.exists(dir.resolve("bin/java")) &&
                                !Files.exists(dir.resolve("bin/java.exe")))
                                return false;

                            // analyze directory name
                            StringReader reader = new StringReader(dir.getFileName().toString());

                            String type = reader.read(3);
                            if (this.type != Type.ANY && !this.type.name().equalsIgnoreCase(type))
                                return false;

                            if (reader.curr() == '-')
                                reader.next();

                            String versionStr = reader.collect();
                            JavaVersion version = JavaVersion.fromString(versionStr);

                            // check version
                            return this.version.isMajor() ?
                                    version.getMajor() == this.version.getMajor() :
                                    version.equals(this.version);
                        })
                        .findAny();

                if (optionalInstallationPath.isPresent()) {
                    Path validInstallationPath = optionalInstallationPath.get();

                    // create text file with the installation path
                    Files.createDirectories(path);
                    FileUtil.overwriteWithString(path.resolve("installation"), validInstallationPath.toAbsolutePath().toString());

                    // create and return local package
                    return CompletableFuture.completedFuture(
                            new LocalJavaPackage(manager, this, path, validInstallationPath)
                    );
                }
            }

            // throw error, maybe later actually
            // download and install a jdk from the internet?
            throw new IllegalStateException("Could not find a " + type + version + " installation");
        } catch (IOException e) {
            e.printStackTrace();
            Throwables.sneakyThrow(e);
            throw new AssertionError();
        }
    }

    @Override
    public LocalJavaPackage loadLocally(PackageManager manager, Path path) {
        try {
            // read installation path
            String installationPathStr = FileUtil.readString(path.resolve("installation"));
            Path installationPath = Paths.get(installationPathStr);

            return new LocalJavaPackage(manager, this, path, installationPath);
        } catch (IOException e) {
            Throwables.sneakyThrow(e);
            throw new AssertionError();
        }
    }

}
