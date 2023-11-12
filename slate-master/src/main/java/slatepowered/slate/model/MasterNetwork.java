package slatepowered.slate.model;

import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;

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

}
