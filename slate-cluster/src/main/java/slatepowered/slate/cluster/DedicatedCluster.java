package slatepowered.slate.cluster;

import lombok.Getter;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.slate.allocation.ClusterAllocationChecker;
import slatepowered.slate.allocation.LocalNodeAllocation;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;
import slatepowered.slate.packages.PackageManager;
import slatepowered.veru.misc.Throwables;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A dedicated, standalone cluster for node allocations across
 * multiple instances/networks.
 */
@Getter
//@Builder // todo: fix this annotation
public class DedicatedCluster extends Cluster<DedicatedClusterInstance> {

    /**
     * The local package manager.
     */
    private final PackageManager packageManager;

    /**
     * The cluster instances.
     */
    private final List<ClusterInstance> instances = new ArrayList<>();

    /**
     * All local node allocations.
     */
    private final List<LocalNodeAllocation> allocations = new ArrayList<>();

    /**
     * The allocation checker.
     */
    private final ClusterAllocationChecker allocationChecker;

    /**
     * The communication strategy.
     */
    private final CommunicationStrategy communicationStrategy;

    // the communication provider for clusterDeclares
    private RPCManager clusterDeclareRPC;

    /**
     * The working/data directory for this cluster.
     */
    private final Path directory;

    public DedicatedCluster(String name, PackageManager packageManager, ClusterAllocationChecker allocationChecker, CommunicationStrategy communicationStrategy, Path directory) {
        super(name);
        this.packageManager = packageManager;
        this.allocationChecker = allocationChecker;
        this.communicationStrategy = communicationStrategy;
        this.directory = directory;
    }

    /**
     * Starts and initializes this cluster.
     */
    @Override
    public void start() {
        try {
            clusterDeclareRPC = communicationStrategy
                    .getRPCManager(CommunicationKey.clusterDeclare());

            clusterDeclareRPC.register(new ClusterInstantiationAPI() {
                @Override
                public void declareClusterInstance(CommunicationKey communicationKey) {
                    getOrCreateInstance(communicationKey);
                }
            });
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
        }
    }

    @Override
    public void close() {
        
    }

    @Override
    public DedicatedClusterInstance createInstance(CommunicationKey key) {
        return new DedicatedClusterInstance(this, key, communicationStrategy);
    }

    @Override
    public Path getInstanceDirectory(ClusterInstance instance) {
        return directory.resolve("instances").resolve(Integer.toHexString(instance.getCommunicationKey().hashCode()));
    }

    @Override
    public Path getClusterDirectory() {
        return directory;
    }

    /**
     * Get the local package manager.
     *
     * @return The manager.
     */
    public PackageManager getLocalPackageManager() {
        return packageManager;
    }

    /**
     * Get the instances managed by this cluster.
     *
     * @return The list of instances.
     */
    public List<ClusterInstance> getInstances() {
        return instances;
    }

    public ClusterAllocationChecker getAllocationChecker() {
        return allocationChecker;
    }

    /**
     * Get all node allocations/creations on this cluster,
     * irregardless of the instance/network it belongs to.
     *
     * @return The allocations.
     */
    public List<LocalNodeAllocation> getAllocations() {
        return allocations;
    }

    public Path getDirectory() {
        return directory;
    }

}
