package slatepowered.slate.model;

/**
 * The virtual node which represents the master node locally on the master.
 */
final class MasterNode extends MasterManagedNode {

    /* The master node should have all permissions. */
    private static final String[] TAGS = new String[] { "node", "master", "controller", "*" };

    public MasterNode(String name, MasterNetwork network) {
        super(/* the master node does not have a parent */ null, name, network, null);
    }

    @Override
    public String[] getTags() {
        return TAGS;
    }

    @Override
    public String remoteChannelName() {
        return "master";
    }

}
