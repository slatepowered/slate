package slatepowered.slate.packages.resolved;

import lombok.RequiredArgsConstructor;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.ResolvedPackage;
import slatepowered.slate.packages.local.LocalFilesPackage;
import slatepowered.slate.packages.service.ProvidedPackageService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * A package provided by the master controller of a network.
 */
@RequiredArgsConstructor
public class MasterProvidedFilesPackage<K extends PackageKey<LocalFilesPackage>> extends ResolvedPackage<K, LocalFilesPackage> {

    /**
     * The package key.
     */
    private final K key;

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public CompletableFuture<LocalFilesPackage> installLocally(PackageManager manager, Path path) {
        return manager
                .getServiceManager().getService(ProvidedPackageService.KEY)
                .downloadPackage(manager, this, path)
                .thenApply(p -> new LocalFilesPackage(manager, this, p));
    }

    @Override
    public LocalFilesPackage loadLocally(PackageManager manager, Path path) {
        if (!Files.exists(path))
            return null;
        return new LocalFilesPackage(manager, this, path);
    }
}
