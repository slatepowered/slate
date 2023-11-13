package slatepowered.slate.cluster;

import lombok.RequiredArgsConstructor;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.master.Master;
import slatepowered.slate.packages.PackageManager;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The integrated cluster provided by the master.
 */
public class IntegratedCluster extends Cluster<IntegratedClusterInstance> {

    /**
     * The master instance.
     */
    protected final Master master;

    // The singular integrated cluster instance
    protected IntegratedClusterInstance theInstance;

    public IntegratedCluster(String name, Master master) {
        super(name);
        this.master = master;
    }

    @Override
    public PackageManager getLocalPackageManager() {
        return master.getLocalPackageManager();
    }

    @Override
    public void start() {

    }

    @Override
    public void close() {

    }

    @Override
    public IntegratedClusterInstance getInstance(CommunicationKey key) {
        return theInstance;
    }

    @Override
    public IntegratedClusterInstance createInstance(CommunicationKey key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getInstanceDirectory(ClusterInstance instance) {
        return Paths.get("./cluster");
    }

}
