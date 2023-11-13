package slatepowered.slate.packages;

import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceKey;
import slatepowered.veru.misc.Throwables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Locally manages and caches packages.
 */
public class PackageManager implements Service {

    private static final Logger LOGGER = Logger.getLogger("PackageManager");

    public static final ServiceKey<PackageManager> KEY = ServiceKey.local(PackageManager.class);

    /**
     * The cached local packages by package key.
     */
    protected final Map<PackageKey, LocalPackage> localPackageMap = new ConcurrentHashMap<>();

    /**
     * The cached resolved packages.
     */
    protected final Map<PackageKey, ResolvedPackage> resolvedPackageCache = new ConcurrentHashMap<>();

    /**
     * The local package directory.
     */
    protected final Path directory;

    public PackageManager(Path directory) {
        this.directory = directory;

        try {
            Files.createDirectories(directory);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialize local package manager dir(" + directory + ")", t);
        }
    }

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
     * Newly resolve the given package key.
     *
     * @param key The package key.
     * @return The result future.
     */
    protected CompletableFuture<ResolvedPackage> resolvePackage0(PackageKey key) {
        // todo
        return null;
    }

    /**
     * Find the given key in the cache or newly resolve the given package key.
     *
     * @param key The package key.
     * @return The resolved package future.
     */
    public CompletableFuture<ResolvedPackage> resolvePackage(PackageKey key) {
        ResolvedPackage resolvedPackage = resolvedPackageCache.get(key);
        if (resolvedPackage != null) {
            return CompletableFuture.completedFuture(resolvedPackage);
        }

        CompletableFuture<ResolvedPackage> future = resolvePackage0(key);
        future.whenComplete(((resolved, throwable) -> {
            if (throwable != null) {
                LOGGER.warning("Failed to resolve package by key: " + key);
                throwable.printStackTrace();
                return;
            }

            resolvedPackageCache.put(key, resolved);

            // store this result in the package instance
            final LocalPackage localPackage = getCachedPackage(key);
            if (localPackage != null) {
                localPackage.resolvedKey(resolved);
            }
        }));

        return future;
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
     * @param resolvedKey The resolved key.
     * @return The future for the local package.
     */
    public CompletableFuture<LocalPackage> findOrInstallPackage(ResolvedPackage resolvedKey) {
        PackageKey key = resolvedKey.getKey();
        LocalPackage localPackage = findPackage(key);
        if (localPackage == null) {
            // install package
            return resolvedKey.installLocally(
                    this.directory.resolve(key.toUUID().toString())
            );
        } else if (localPackage.getResolvedKey() == null) {
            // set the resolved key for the package
            localPackage.resolvedKey(resolvedKey);
        }

        return CompletableFuture.completedFuture(localPackage);
    }

    /**
     * Find the given package if locally cached or else
     * try and install the package locally.
     *
     * @param key The key.
     * @return The future for the local package.
     */
    public CompletableFuture<LocalPackage> findOrInstallPackage(PackageKey key) {
        return resolvePackage(key).thenCompose(this::findOrInstallPackage);
    }

}
