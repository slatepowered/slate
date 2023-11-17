package slatepowered.slate.packages.service;

import slatepowered.reco.rpc.Local;
import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.resolved.MasterProvidedFilesPackage;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.network.NetworkServiceKey;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * The network service for packages provided by the master controller.
 */
public interface ProvidedPackageService extends Service, RemoteAPI {

    NetworkServiceKey<ProvidedPackageService> KEY = NetworkServiceKey.fromMaster(ProvidedPackageService.class);

    /**
     * Download the given package from the master to the appropriate directory
     * and with the appropriate properties given by the package manager provided.
     *
     * @param packageManager The local package manager.
     * @param providedPackage The package to install.
     * @param file The output file.
     * @return The result future.
     */
    @Local
    default CompletableFuture<Path> downloadPackage(PackageManager packageManager,
                                                    MasterProvidedFilesPackage<?> providedPackage,
                                                    Path file) {
        return null; // todo
    }

}
