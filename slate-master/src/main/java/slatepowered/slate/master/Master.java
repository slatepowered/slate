package slatepowered.slate.master;

import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.ProvidedChannel;
import slatepowered.slate.model.MasterManagedNode;
import slatepowered.slate.model.MasterNetwork;
import slatepowered.slate.model.services.NetworkInfoService;
import slatepowered.veru.data.Pair;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * The master bootstrap/network.
 */
public class Master extends MasterNetwork {

    Master(CommunicationProvider<? extends ProvidedChannel> communicationProvider) {
        super(communicationProvider);

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

        private CommunicationProvider<?> communicationProvider;

        public MasterBuilder communicationProvider(CommunicationProvider<?> communicationProvider) {
            this.communicationProvider = communicationProvider;
            return this;
        }

        public Master build() {
            return new Master(communicationProvider);
        }

    }

    public static MasterBuilder builder() {
        return new MasterBuilder();
    }

}
