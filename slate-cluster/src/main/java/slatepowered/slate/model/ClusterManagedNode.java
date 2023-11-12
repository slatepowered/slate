package slatepowered.slate.model;

import lombok.Getter;
import lombok.Setter;
import slatepowered.slate.allocation.LocalNodeAllocation;

import java.util.List;

public abstract class ClusterManagedNode extends ManagedNode {

    @Setter
    @Getter
    protected LocalNodeAllocation allocation;

    /**
     * The tags for this node.
     */
    protected final String[] tags;

    public ClusterManagedNode(Node parent, String name, Network<?> network, List<NodeComponent> components, String[] tags) {
        super(parent, name, network, components);
        this.tags = tags;
    }

    @Override
    public String[] getTags() {
        return tags;
    }

}
