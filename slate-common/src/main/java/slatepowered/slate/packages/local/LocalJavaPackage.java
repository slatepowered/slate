package slatepowered.slate.packages.local;

import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.ResolvedPackage;
import slatepowered.veru.misc.Throwables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents a local Java package, identified by a {@link slatepowered.slate.packages.key.JavaPackageKey}.
 */
public class LocalJavaPackage extends LocalPackage {

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

    public Path getInstallationPath() {
        return installationPath;
    }

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
