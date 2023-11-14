package slatepowered.slate.packages;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Identifies a package.
 *
 * @param <P> The local package type.
 */
public interface PackageKey<P extends LocalPackage> {

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

    @SuppressWarnings("unchecked")
    default CompletableFuture<P> findOrInstall(PackageManager manager) {
        return manager.findOrInstallPackage(this);
    }

    /**
     * Utility: convert the given string to a valid UUID
     */
    static UUID stringToUUID(String str) {
        return UUID.nameUUIDFromBytes(str.getBytes(StandardCharsets.UTF_8));
    }

}
