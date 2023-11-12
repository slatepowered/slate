package slatepowered.slate.model;

import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.ProvidedChannel;

/**
 * A network mirror instance managed by the local cluster.
 */
public class ClusterNetwork extends Network<Node> {

    /**
     * The master node.
     */
    private final ClusterManagedNode masterNode;

    public ClusterNetwork(CommunicationProvider<? extends ProvidedChannel> communicationProvider) {
        super(communicationProvider);

        // create the master node
        this.masterNode = new ClusterManagedNode(null, "master", this, null, new String[] { "*", "all", "master" }) { };
    }

    @Override
    public ClusterManagedNode master() {
        return masterNode;
    }

}
