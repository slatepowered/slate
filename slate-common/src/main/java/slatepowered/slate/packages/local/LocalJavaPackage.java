package slatepowered.slate.packages.local;

import slatepowered.slate.model.NodeComponent;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.ResolvedPackage;
import slatepowered.slate.packages.key.JavaPackageKey;
import slatepowered.veru.misc.Throwables;
import slatepowered.veru.runtime.JavaVersion;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a local Java package, identified by a {@link slatepowered.slate.packages.key.JavaPackageKey}.
 */
public class LocalJavaPackage extends LocalPackage implements NodeComponent {

    /**
     * The path to the Java installation.
     */
    private final Path installationPath;

    // The cached Java executable
    private Path javaBinary;

    public LocalJavaPackage(PackageManager manager, ResolvedPackage<?, LocalJavaPackage> key, Path path, Path installationPath) {
        super(manager, key, path);
        this.installationPath = installationPath;
    }

    /**
     * Get the Java version this package represents.
     *
     * @return The version.
     */
    public JavaVersion getVersion() {
        return this.<JavaPackageKey>getKey().getVersion();
    }

    /**
     * Get the path to the local installation of the JDK/JRE.
     *
     * @return The installation path.
     */
    public Path getInstallationPath() {
        return installationPath;
    }

    /**
     * Get the path to the Java executable.
     *
     * @return The Java executable.
     */
    public Path getJavaBinary() {
        if (javaBinary == null) {
            try {
                javaBinary = installationPath.resolve("bin/java");
                if (!Files.exists(javaBinary))
                    javaBinary = installationPath.resolve("bin/java.exe");
            } catch (Throwable t) {
                Throwables.sneakyThrow(t);
                throw new AssertionError();
            }
        }

        return javaBinary;
    }

}
