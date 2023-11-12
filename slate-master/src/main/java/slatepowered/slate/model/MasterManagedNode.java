package slatepowered.slate.model;

import slatepowered.slate.action.InitializationResult;
import slatepowered.slate.action.NodeDestroyAdapter;
import slatepowered.slate.action.NodeInitializeAdapter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implementation of {@link ManagedNode} on the master instance, with
 * full control permissions.
 */
public abstract class MasterManagedNode extends ManagedNode {

    public MasterManagedNode(MasterManagedNode parent, String name, Network<Node> network, List<NodeComponent> components) {
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

    // run the action through the components
    @SuppressWarnings("unchecked")
    private <T, C extends NodeComponent> CompletableFuture<T> runAction(Class<C> component, BiFunction<C, ManagedNode, ?> function, Function<Throwable, T> tFunction) {
        CompletableFuture<Void>[] futures = (CompletableFuture<Void>[]) findComponents(component)
                .stream()
                .map(c -> function.apply(c, this))
                .toArray();

        if (tFunction != null) {
            CompletableFuture<T> future = new CompletableFuture<>();
            CompletableFuture.allOf(futures).whenComplete((__, t) -> future.complete(tFunction.apply(t)));
            return future;
        } else {
            return (CompletableFuture<T>) CompletableFuture.allOf(futures);
        }
    }

    /**
     * Initialize this node.
     *
     * @return The initialization result future.
     */
    public CompletableFuture<InitializationResult> initialize() {
        CompletableFuture<InitializationResult> future =
                this.runAction(NodeInitializeAdapter.class, NodeInitializeAdapter::create,
                        t -> t == null ? new InitializationResult(false, null) : new InitializationResult(true, Collections.singletonList(t)));
        future.whenComplete((result, err) -> {
            if (!result.isSuccess()) {
                LOGGER.warning("Failed to initialize node(" + this.name + ") with " + result.getErrors().size() + " errors");
                result.getErrors().forEach(Throwable::printStackTrace);
                return;
            }

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
                this.runAction(NodeDestroyAdapter.class, NodeDestroyAdapter::destroy, null);
        future.whenComplete((unused, err) -> {
            if (err != null) {
                LOGGER.warning("Destruction of node(" + this.name + ") encountered an error while running action");
                err.printStackTrace();
            }

            // unregister this node
            network.nodeMap.remove(this.name);
            if (parent instanceof ManagedNode) {
                ((ManagedNode)parent).children.remove(this.name);
            }

            LOGGER.info("Destroyed node(" + this.name + ")");
        });

        return future;
    }

}
