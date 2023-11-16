package slatepowered.slate.packages;

import slatepowered.slate.packages.key.TrivialPackageKey;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceKey;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Locally manages and caches packages.
 */
@SuppressWarnings("rawtypes")
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

    /**
     * The list of package resolvers.
     */
    protected final List<PackageResolver> packageResolvers = new ArrayList<>();

    public PackageManager(Path directory) {
        this.directory = directory;

        try {
            Files.createDirectories(directory);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialize local package manager dir(" + directory + ")", t);
        }
    }

    /**
     * Registers each resolver in the given varargs array
     * as a package resolver for this package manager.
     *
     * @param resolvers The resolvers to register.
     * @return This.
     */
    public PackageManager with(PackageResolver... resolvers) {
        packageResolvers.addAll(Arrays.asList(resolvers));
        return this;
    }

    /**
     * Get a cached package for the given key.
     *
     * @param key The key.
     * @return The cached local package or null if absent/not cached.
     */
    @SuppressWarnings("unchecked")
    public <P extends LocalPackage> P getCachedPackage(PackageKey<P> key) {
        return (P) localPackageMap.get(key);
    }

    /**
     * Newly resolve the given package key.
     *
     * @param key The package key.
     * @return The result future.
     */
    @SuppressWarnings("unchecked")
    protected <P extends LocalPackage> CompletableFuture<ResolvedPackage<?, P>> resolvePackage0(PackageKey<P> key) {
        if (key instanceof TrivialPackageKey) {
            CompletableFuture<ResolvedPackage<?, P>> future = ((TrivialPackageKey<P>)key).resolve(this);
            if (future != null) {
                return future;
            }
        }

        // try the resolvers
        for (PackageResolver resolver : packageResolvers) {
            if (resolver.canResolve(this, key)) {
                CompletableFuture<ResolvedPackage<?, ?>> future =
                        resolver.tryResolve(this, key);
                if (future != null) {
                    return (CompletableFuture<ResolvedPackage<?,P>>)(Object) future;
                }
            }
        }

        throw new IllegalStateException("Failed to resolve package `" + key + "`");
    }

    /**
     * Find the given key in the cache or newly resolve the given package key.
     *
     * @param key The package key.
     * @return The resolved package future.
     */
    @SuppressWarnings("unchecked")
    public <P extends LocalPackage> CompletableFuture<ResolvedPackage<?, P>> resolvePackage(PackageKey<P> key) {
        ResolvedPackage<?, P> resolvedPackage = resolvedPackageCache.get(key);
        if (resolvedPackage != null) {
            return CompletableFuture.completedFuture(resolvedPackage);
        }

        CompletableFuture<ResolvedPackage<?, P>> future = resolvePackage0(key);
        future.whenComplete(((resolved, throwable) -> {
            if (throwable != null) {
                LOGGER.warning("Failed to resolve package by key: " + key);
                throwable.printStackTrace();
                return;
            }

            resolvedPackageCache.put(key, resolved);

            // store this result in the package instance
            final LocalPackage localPackage = getCachedPackage(key);
        }));

        return future;
    }

    /**
     * Find or load a locally installed package.
     *
     * @param key The key.
     * @return The package or null if absent.
     */
    @SuppressWarnings("unchecked")
    public <P extends LocalPackage> P findOrLoadPackage(PackageKey<P> key) {
        P localPackage = getCachedPackage(key);
        // todo: currently joining the resolving process, idk if that's good
        return localPackage != null ? localPackage : findOrLoadPackage(resolvePackage(key).join());
    }

    /**
     * Find or load a locally installed package by resolved key.
     *
     * @param resolvedKey The resolved key.
     * @return The package or null if absent.
     */
    @SuppressWarnings("unchecked")
    public <P extends LocalPackage> P findOrLoadPackage(ResolvedPackage<?, P> resolvedKey) {
        PackageKey<P> key = resolvedKey.getKey();
        P localPackage = getCachedPackage(key);

        if (localPackage == null) {
            String dirName = key.toUUID().toString();
            Path path = directory.resolve(dirName);

            // try to load data to local package, this should
            // check whether the path exists itself if that's needed;
            // for dynamic packages this should working without an existing
            // file or directory
            localPackage = resolvedKey.loadLocally(this, path);
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
    public <P extends LocalPackage> CompletableFuture<P> findOrInstallPackage(ResolvedPackage<?, P> resolvedKey) {
        PackageKey<P> key = resolvedKey.getKey();
        P localPackage = findOrLoadPackage(resolvedKey);
        if (localPackage == null) {
            // install package
            return resolvedKey.installLocally(
                    this,
                    this.directory.resolve(key.toUUID().toString())
            ).exceptionally(throwable -> {
                throw new RuntimeException("Failed to install package " + resolvedKey, throwable);
            });
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
    public <P extends LocalPackage> CompletableFuture<P> findOrInstallPackage(PackageKey<P> key) {
        return resolvePackage(key).thenCompose(this::findOrInstallPackage);
    }

}
