package slatepowered.slate.model;

import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.ProvidedChannel;

/**
 * The master managed implementation of {@link Network}.
 */
public class MasterNetwork extends Network<MasterManagedNode> {

    /** The master node. */
    private final MasterNode masterNode = new MasterNode("master", this);

    public MasterNetwork(CommunicationProvider<? extends ProvidedChannel> communicationProvider) {
        super(communicationProvider);
    }

    @Override
    public MasterManagedNode master() {
        return masterNode;
    }

}
