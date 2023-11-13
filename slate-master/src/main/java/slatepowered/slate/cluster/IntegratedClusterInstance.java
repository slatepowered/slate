package slatepowered.slate.cluster;

import slatepowered.slate.allocation.ClusterAllocationChecker;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;
import slatepowered.slate.model.Node;

/**
 * The integrated cluster instance.
 */
public class IntegratedClusterInstance extends ClusterInstance {

    /**
     * The allocation checker.
     */
    protected ClusterAllocationChecker allocationChecker = (cluster, clusterInstance, name, tags) -> true;

    public IntegratedClusterInstance(IntegratedCluster cluster, CommunicationKey communicationKey, CommunicationStrategy communicationStrategy) {
        super(cluster, communicationKey, communicationStrategy);

        if (cluster.theInstance != null)
            throw new IllegalStateException("Attempt to construct second cluster instance on an integrated cluster");
        cluster.theInstance = this;
    }

    /**
     * Get the node representing this integrated cluster.
     *
     * @return The node.
     */
    public Node node() {
        return local();
    }

    /**
     * Set the allocation checker for this integrated cluster.
     *
     * @param allocationChecker The allocation checker.
     * @return This.
     */
    public IntegratedClusterInstance setAllocationChecker(ClusterAllocationChecker allocationChecker) {
        this.allocationChecker = allocationChecker;
        return this;
    }

    @Override
    public ClusterAllocationChecker getAllocationChecker() {
        return allocationChecker;
    }

    @Override
    public Node local() {
        return null;
    }
}
