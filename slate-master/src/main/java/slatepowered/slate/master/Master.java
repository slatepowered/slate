package slatepowered.slate.master;

import slatepowered.reco.rpc.event.RemoteEvent;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;
import slatepowered.slate.model.MasterManagedNode;
import slatepowered.slate.model.MasterNetwork;
import slatepowered.slate.network.NetworkInfoService;
import slatepowered.veru.data.Pair;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * The master bootstrap/network.
 */
public class Master extends MasterNetwork {

    Master(CommunicationKey communicationKey, CommunicationStrategy<CommunicationKey> communicationStrategy) {
        super(communicationKey, communicationStrategy);
    }

    /**
     * Closes the network, this should destroy all services.
     */
    public void close() {
        rpcManager.invokeRemoteEvent(NetworkInfoService.class, "onClose", null);

        // finally, close the communication provider
        // this basically closes the network until
        // a whole new network instance is created
        communicationProvider.close();
    }

    {
        // create default services
        serviceManager.register(NetworkInfoService.key(), new NetworkInfoService() {
            @Override
            public Collection<Pair<String, String[]>> fetchNodeNames() {
                return Master.this.getNodeMap().values().stream()
                        .map(n -> Pair.of(n.getName(), n.getTags()))
                        .collect(Collectors.toList());
            }

            @Override
            public NodeInfo fetchNodeInfo(String name) {
                MasterManagedNode node = Master.this.getNode(name);
                return new NodeInfo(node.getName(), node.getParent().getName(), node.getTags());
            }
        });
    }

    /**
     * Builds a network master on this JVM.
     */
    public static class MasterBuilder {

        private CommunicationKey communicationKey;
        private CommunicationStrategy<CommunicationKey> communicationStrategy;

        public MasterBuilder communicationKey(CommunicationKey communicationKey) {
            this.communicationKey = communicationKey;
            return this;
        }

        public MasterBuilder communicationStrategy(CommunicationStrategy<CommunicationKey> communicationStrategy) {
            this.communicationStrategy = communicationStrategy;
            return this;
        }

        public Master build() {
            return new Master(communicationKey, communicationStrategy);
        }

    }

    public static MasterBuilder builder() {
        return new MasterBuilder();
    }

}
