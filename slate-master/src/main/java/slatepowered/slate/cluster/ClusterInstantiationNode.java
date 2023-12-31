package slatepowered.slate.cluster;

import slatepowered.reco.rpc.RPCManager;
import slatepowered.slate.allocation.NodeAllocator;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.model.NodeComponent;
import slatepowered.slate.model.NodeHost;

/**
 * Node component which instantiates and attaches a cluster instance to this node.
 */
public class ClusterInstantiationNode implements NodeComponent {

    @Override
    public boolean attached(ManagedNode node) {
        String clusterName = node.getName();
        RPCManager rpcManager = node.getSingleton(RPCManager.class);

        RPCManager clusterDeclareRPC = node.getNetwork()
                .getCommunicationStrategy()
                .getRPCManager(CommunicationKey.clusterDeclare());

        ClusterInstantiationAPI api = clusterDeclareRPC.bindRemote(clusterDeclareRPC.getLocalChannel().provider().channel(clusterName),
                ClusterInstantiationAPI.class);
        api.declareClusterInstance(node.getNetwork().getCommunicationKey());

        node.attach(new ClusterInstanceNode());

        // attach remote node allocator
        node.register(NodeAllocator.KEY, rpcManager.bindRemote(node.getChannel(), NodeAllocator.class));

        return false; // dont attach this component
    }

}
