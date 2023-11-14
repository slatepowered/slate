package slatepowered.slate.packages.key;


import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.ResolvedPackage;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A pre-resolved package and package key union.
 */
public abstract class ResolvedPackageKeyUnion<K extends ResolvedPackageKeyUnion<K, P>, P extends LocalPackage> extends ResolvedPackage<K, P> implements TrivialPackageKey<P> {

    // The cached UUID
    protected transient UUID uuid;

    /** Creates a new UUID to represent this package */
    protected abstract UUID makeUUID();

    @Override
    public UUID toUUID() {
        if (uuid == null) {
            uuid = makeUUID();
        }

        return uuid;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K getKey() {
        return (K) this;
    }

    @Override
    public CompletableFuture<ResolvedPackage<?, P>> resolve(PackageManager manager) {
        return CompletableFuture.completedFuture(this);
    }

}
