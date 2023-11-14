package slatepowered.slate.packages;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * A resolved package key with installation steps and other
 * useful data.
 */
public abstract class ResolvedPackage<K extends PackageKey<P>, P extends LocalPackage> {

    /**
     * Get the package key this object resolves.
     *
     * @return The package key.
     */
    public abstract K getKey();

    /**
     * Locally install this package to the given directory
     * and create a {@link LocalPackage} instance. At this time
     * the directory is not created yet.
     *
     * @param manager The package manager.
     * @param path The directory to install it to.
     * @return The future.
     */
    public abstract CompletableFuture<P> installLocally(PackageManager manager, Path path);

    /**
     * Load the files in the given directory into a local package for
     * the given manager.
     *
     * @param manager The manager.
     * @param path The package directory.
     * @return The local package instance.
     */
    public abstract P loadLocally(PackageManager manager, Path path);

}
