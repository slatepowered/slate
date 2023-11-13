package slatepowered.slate.cluster;

import lombok.Builder;
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
 * Locally manages clusters for any network which requests it,
 * and provides shared data like local packages to be used by
 * any network.
 */
@Getter
@Builder
public class Cluster {

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

    /**
     * Starts and initializes this cluster.
     */
    public void start() {
        try {
            clusterDeclareRPC = communicationStrategy
                    .getRPCManager(CommunicationKey.clusterDeclare());

            clusterDeclareRPC.register(new ClusterInstantiationAPI() {
                @Override
                public void declareClusterInstance(CommunicationKey communicationKey) {
                    ClusterInstance instance = instance()
                            .communicationStrategy(communicationStrategy)
                            .communicationKey(communicationKey)
                            .build();
                }
            });
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
        }
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

    public ClusterInstance.ClusterInstanceBuilder instance() {
        return ClusterInstance.builder(this);
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
