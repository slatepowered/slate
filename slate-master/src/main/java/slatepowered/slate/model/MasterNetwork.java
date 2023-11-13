package slatepowered.slate.model;

import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;
import slatepowered.slate.network.NetworkInfoService;

/**
 * The master managed implementation of {@link Network}.
 */
public class MasterNetwork extends Network<MasterManagedNode> {

    /** The master node. */
    private final MasterNode masterNode = new MasterNode("master", this);

    public MasterNetwork(CommunicationKey communicationKey, CommunicationStrategy<CommunicationKey> communicationStrategy) {
        super(communicationKey, communicationStrategy);
    }

    @Override
    public MasterManagedNode master() {
        return masterNode;
    }

    // called when the given node is successfully
    // initialized
    protected void onNodeInitialize(MasterManagedNode node) {
        rpcManager.invokeRemoteEvent(NetworkInfoService.class, "onNodeInitialize",
                new NetworkInfoService.NodeInfo(node.getName(), node.getParent().getName(), node.getTags()));
    }

    // destroy the given node and call
    // the network event
    protected void destroyNode(MasterManagedNode node) {
        nodeMap.remove(node.getName());
        if (node.getParent() instanceof ManagedNode) {
            ((ManagedNode)node.getParent()).children.remove(node.getName());
        }

        // call event
        rpcManager.invokeRemoteEvent(NetworkInfoService.class, "onNodeDestroy", node.getName());
    }

}
