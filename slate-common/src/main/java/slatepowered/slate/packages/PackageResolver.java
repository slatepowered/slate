package slatepowered.slate.packages;

import java.util.concurrent.CompletableFuture;

/**
 * Locally resolves package keys for more information related
 * to installation and linking.
 */
public interface PackageResolver {

    /**
     * Estimate whether this resolver can reliably resolve
     * the given package key.
     *
     * @param manager The local package manager.
     * @param key The key to resolve.
     * @return Whether the key is supported.
     */
    boolean canResolve(PackageManager manager,
                       PackageKey<?> key);

    /**
     * Try and resolve the given package key if supported by
     * this resolver.
     *
     * If null is returned, this is interpreted as the key not being
     * supported by this package resolver so it continues it's search
     * for a package resolver, otherwise the result {@link ResolvedPackage}
     * is awaited and accepted as the resolved package for the key.
     *
     * @param manager The local package manager.
     * @param key The package key to resolve.
     * @return The resolved package future or null if unsupported.
     */
    CompletableFuture<ResolvedPackage<?, ?>> tryResolve(PackageManager manager,
                                                        PackageKey<?> key);

}
