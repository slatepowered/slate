package slatepowered.slate.model;

import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;

/**
 * A network mirror instance managed by the local cluster.
 */
public abstract class ClusterNetwork extends Network {

    /**
     * The master node.
     */
    private final Node masterNode;

    public ClusterNetwork(CommunicationKey communicationKey, CommunicationStrategy communicationStrategy) {
        super(communicationKey, communicationStrategy);

        // create the master node
        this.masterNode = Node.masterNode(this);
    }

    @Override
    public Node master() {
        return masterNode;
    }

    @Override
    public Node local() {
        return null;
    }

}
