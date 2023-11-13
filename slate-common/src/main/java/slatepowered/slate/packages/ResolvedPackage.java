package slatepowered.slate.packages;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A resolved package key with installation steps and other
 * useful data.
 */
public abstract class ResolvedPackage {

    protected final PackageKey key;

    protected ResolvedPackage(PackageKey key) {
        this.key = key;
    }

    public PackageKey getKey() {
        return key;
    }

    /**
     * Locally install this package to the given
     * directory.
     *
     * @param path The directory to install it to.
     * @return The future.
     */
    public abstract CompletableFuture<LocalPackage> installLocally(Path path);

}
