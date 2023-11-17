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
    default String getProvider() {
        return null;
    }

    /**
     * Get the operational package key which should be used for local operations.
     *
     * @return The base package key.
     */
    default PackageKey<P> baseKey() {
        return this;
    }

    /**
     * Get the identifier for the resources of this package.
     *
     * @return The identifier.
     */
    default String getIdentifier() {
        return toUUID().toString();
    }

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

    /**
     * Create a named package key wrapping the given key type.
     *
     * @param key The key.
     * @param name The name.
     * @param <P> The package type.
     * @return The named package key.
     */
    static <P extends LocalPackage> PackageKey<P> wrapNamed(PackageKey<P> key,
                                                            String name) {
        return new PackageKey<P>() {
            // The cached UUID of this package key
            private final UUID uuid = stringToUUID(name);

            @Override
            public UUID toUUID() {
                return uuid;
            }

            @Override
            public PackageKey<P> baseKey() {
                return key;
            }

            @Override
            public String toString() {
                return name;
            }

            @Override
            public String getIdentifier() {
                return name;
            }
        };
    }

    /**
     * Create a named package key.
     *
     * @param name The name.
     * @param <P> The package type.
     * @return The named package key.
     */
    static <P extends LocalPackage> PackageKey<P> named(String name) {
        return new PackageKey<P>() {
            // The cached UUID of this package key
            private final UUID uuid = stringToUUID(name);

            @Override
            public UUID toUUID() {
                return uuid;
            }

            @Override
            public String toString() {
                return name;
            }

            @Override
            public String getIdentifier() {
                return name;
            }
        };
    }

}
