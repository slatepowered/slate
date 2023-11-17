package slatepowered.slate.model;

import slatepowered.slate.action.InitializationResult;
import slatepowered.slate.action.NodeDestroyAdapter;
import slatepowered.slate.action.NodeInitializeAdapter;
import slatepowered.slate.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link ManagedNode} on the master instance, with
 * full control permissions.
 */
public abstract class MasterManagedNode extends ManagedNode {

    public MasterManagedNode(MasterManagedNode parent, String name, MasterNetwork network, List<NodeComponent> components) {
        super(parent, name, network, components);
    }

    /**
     * Return a node builder for a child with the given name.
     *
     * @param name The name.
     * @return The builder.
     */
    public NodeBuilder child(String name) {
        return new NodeBuilder(this, name);
    }

    /**
     * Initialize this node.
     *
     * @return The initialization result future.
     */
    public CompletableFuture<MasterManagedNode> initialize() {
        LOGGER.debug("Initializing node `", name, "` on master");
        CompletableFuture<MasterManagedNode> future =
                this.<MasterManagedNode, NodeInitializeAdapter>
                        runVoidAction(NodeInitializeAdapter.class, NodeInitializeAdapter::create, null)
                        .thenApply(node1 -> {
                            MasterNetwork masterNetwork = getNetwork();
                            masterNetwork.onNodeInitialize(this);
                            return node1;
                        });

        return future;
    }

    @Override
    public void destroy() {
        LOGGER.debug("Destroying node `", name, "` on master");
        super.destroy();
        CompletableFuture<Void> future =
                this.runVoidAction(NodeDestroyAdapter.class, NodeDestroyAdapter::destroy, null)
                .thenApply(unused -> {
                    MasterNetwork masterNetwork = getNetwork();
                    masterNetwork.destroyNode(this);
                    return null;
                });
    }

}
