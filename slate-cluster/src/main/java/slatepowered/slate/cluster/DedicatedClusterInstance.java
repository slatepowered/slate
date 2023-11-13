package slatepowered.slate.cluster;

import slatepowered.slate.allocation.ClusterAllocationChecker;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;
import slatepowered.slate.model.Node;

/**
 * An instance of a {@link DedicatedCluster} dedicated to one network.
 */
public class DedicatedClusterInstance extends ClusterInstance {

    // The local node
    private final Node localNode;

    public DedicatedClusterInstance(Cluster<?> cluster, CommunicationKey communicationKey, CommunicationStrategy communicationStrategy) {
        super(cluster, communicationKey, communicationStrategy);

        this.localNode = new Node(cluster.getName(), this) {
            final String[] tags = new String[] { "*", "cluster" };

            @Override
            public String[] getTags() {
                return tags;
            }
        };
    }

    @Override
    protected ClusterAllocationChecker getAllocationChecker() {
        return ((DedicatedCluster)cluster).getAllocationChecker();
    }

    @Override
    public Node local() {
        return localNode;
    }

}
