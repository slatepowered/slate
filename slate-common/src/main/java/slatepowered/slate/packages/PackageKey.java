package slatepowered.slate.packages;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Identifies a package.
 */
public interface PackageKey {

    /**
     * Hash this key to a UUID.
     *
     * @return The UUID.
     */
    UUID toUUID();

    /**
     * Get the provider key for this package.
     *
     * @return The provider key.
     */
    String getProvider();

    /**
     * Locally install this package to the given
     * directory.
     *
     * @param path The directory to install it to.
     * @return The future.
     */
    CompletableFuture<LocalPackage> installLocally(Path path);

}
