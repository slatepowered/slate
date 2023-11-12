package slatepowered.slate.allocation;

import slatepowered.slate.cluster.Cluster;
import slatepowered.slate.cluster.ClusterInstance;

public interface ClusterAllocationChecker {

    /**
     * Check whether the node could be allocated on this
     * cluster.
     *
     * @param cluster The cluster.
     * @param clusterInstance The cluster instance.
     * @param name The name of the node to allocate.
     * @param tags The tags of the node.
     * @return Whether the node could be allocated.
     */
    Boolean canAllocate(Cluster cluster, ClusterInstance clusterInstance, String name, String[] tags);

}
