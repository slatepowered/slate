package slatepowered.slate.packages.key;

import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.ResolvedPackage;

import java.util.concurrent.CompletableFuture;

/**
 * A package key which is trivially resolvable to a {@link slatepowered.slate.packages.ResolvedPackage}.
 */
public interface TrivialPackageKey<P extends LocalPackage> extends PackageKey<P> {

    /**
     * Resolves this package key for the given manager.
     *
     * If null is returned the process continues as normal and this
     * trivial package key behavior is ignored.
     *
     * @param manager The local package manager.
     * @return The result future.
     */
    CompletableFuture<ResolvedPackage<?, P>> resolve(PackageManager manager);

}
