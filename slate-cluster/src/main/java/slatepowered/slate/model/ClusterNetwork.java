package slatepowered.slate.model;

import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.ProvidedChannel;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;

/**
 * A network mirror instance managed by the local cluster.
 */
public class ClusterNetwork extends Network<Node> {

    /**
     * The master node.
     */
    private final ClusterManagedNode masterNode;

    public ClusterNetwork(CommunicationKey communicationKey, CommunicationStrategy<CommunicationKey> communicationStrategy) {
        super(communicationKey, communicationStrategy);

        // create the master node
        this.masterNode = new ClusterManagedNode(null, "master", this, null, new String[] { "*", "all", "master" }) { };
    }

    @Override
    public ClusterManagedNode master() {
        return masterNode;
    }

}
