package slatepowered.slate.security;

import slatepowered.reco.ReceivedMessage;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.function.MCallRemote;
import slatepowered.reco.rpc.security.InboundSecurityManager;
import slatepowered.slate.model.Network;

public class NetworkRPCSecurityManager implements InboundSecurityManager {

    /**
     * The network to find the nodes from.
     */
    protected final Network network;

    public NetworkRPCSecurityManager(Network network) {
        this.network = network;
    }

    @Override
    public boolean checkInboundCall(RPCManager manager, ReceivedMessage<MCallRemote> message, String[] securityGroups) {
        return false; // todo
    }

    @Override
    public String[] getSecurityGroups(RPCManager manager, ReceivedMessage<MCallRemote> message) {
        return network.getNode(message.getSource()).getSecurityGroups();
    }

}
