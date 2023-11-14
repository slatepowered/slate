package slatepowered.slate.model;

import slatepowered.slate.action.InitializationResult;
import slatepowered.slate.action.NodeDestroyAdapter;
import slatepowered.slate.action.NodeInitializeAdapter;

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
    public CompletableFuture<InitializationResult> initialize() {
        CompletableFuture<InitializationResult> future =
                this.runVoidAction(NodeInitializeAdapter.class, NodeInitializeAdapter::create,
                        t -> t == null ? new InitializationResult(false, null) : new InitializationResult(true, Collections.singletonList(t)));
        future.whenComplete((result, err) -> {
            if (!result.isSuccess()) {
                LOGGER.warning("Failed to initialize node(" + this.name + ") with " + result.getErrors().size() + " errors");
                result.getErrors().forEach(Throwable::printStackTrace);
                return;
            }

            MasterNetwork masterNetwork = getNetwork();
            masterNetwork.onNodeInitialize(this);
            LOGGER.info("Successfully initialized node(" + this.name + ")");
        });

        return future;
    }

    /**
     * Destroys this node.
     *
     * @return The
     */
    public CompletableFuture<Void> destroy() {
        CompletableFuture<Void> future =
                this.runVoidAction(NodeDestroyAdapter.class, NodeDestroyAdapter::destroy, null);
        future.whenComplete((unused, err) -> {
            if (err != null) {
                LOGGER.warning("Destruction of node(" + this.name + ") encountered an error while running action");
                err.printStackTrace();
            }

            MasterNetwork masterNetwork = getNetwork();
            masterNetwork.destroyNode(this);
            LOGGER.info("Destroyed node(" + this.name + ")");
        });

        return future;
    }

}
