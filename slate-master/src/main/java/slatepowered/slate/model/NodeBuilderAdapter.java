package slatepowered.slate.model;

import lombok.AllArgsConstructor;

/**
 * Base class for anything which extends the node builder.
 */
@AllArgsConstructor
public class NodeBuilderAdapter {

    /**
     * The node builder this adapter is working on.
     */
    protected final NodeBuilder nodeBuilder;

    public NodeBuilder getNodeBuilder() {
        return nodeBuilder;
    }

}
