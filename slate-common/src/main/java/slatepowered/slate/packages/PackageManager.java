package slatepowered.slate.packages;

import lombok.Builder;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceKey;
import slatepowered.veru.misc.Throwables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Locally manages and caches packages.
 */
@Builder
public abstract class PackageManager implements Service {

    public static final ServiceKey<PackageManager> KEY = ServiceKey.local(PackageManager.class);

    /**
     * The cached local packages by package key.
     */
    protected final Map<PackageKey, LocalPackage> localPackageMap = new HashMap<>();

    /**
     * The local package directory.
     */
    protected final Path directory;

    /**
     * Get a cached package for the given key.
     *
     * @param key The key.
     * @return The cached local package or null if absent/not cached.
     */
    public LocalPackage getCachedPackage(PackageKey key) {
        return localPackageMap.get(key);
    }

    /**
     * Find or load a locally installed package.
     *
     * @param key The key.
     * @return The package or null if absent.
     */
    public LocalPackage findPackage(PackageKey key) {
        LocalPackage localPackage = getCachedPackage(key);

        if (localPackage == null) {
            String dirName = key.toUUID().toString();
            Path path = null;

            try {
                path = Files.list(directory)
                        .filter(p -> Files.isDirectory(p) && p.getFileName().toString().equals(dirName))
                        .findFirst()
                        .orElse(null);
            } catch (Throwable t) {
                Throwables.sneakyThrow(t);
            }

            if (path != null) {
                // load data to local package
                localPackage = new LocalPackage(this, key, path);
            }
        }

        return localPackage;
    }

    /**
     * Find the given package if locally cached or else
     * try and install the package locally.
     *
     * @param key The key.
     * @return The future for the local package.
     */
    public CompletableFuture<LocalPackage> findOrInstallPackage(PackageKey key) {
        LocalPackage localPackage = findPackage(key);
        if (localPackage == null) {
            // install package
            return key.installLocally(
                    this.directory.resolve(key.toUUID().toString())
            );
        }

        return CompletableFuture.completedFuture(localPackage);
    }

}
