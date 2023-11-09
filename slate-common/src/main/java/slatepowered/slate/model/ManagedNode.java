package slatepowered.slate.model;

import slatepowered.slate.model.action.CreationResult;
import slatepowered.slate.model.action.NodeCreateAdapter;
import slatepowered.veru.collection.Subset;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a node you have management capabilities over.
 */
public abstract class ManagedNode extends Node {

    /**
     * All components attached to this node.
     */
    protected final List<NodeComponent> components;

    public ManagedNode(String name, Network<Node> network, List<NodeComponent> components) {
        super(name, network);
        this.components = components;
    }

    /**
     * Get all components attached to this node.
     *
     * @return The components.
     */
    public List<NodeComponent> getComponents() {
        return components;
    }

    /**
     * Find all components which are assignable to the given class.
     *
     * @param kl The class.
     * @param <T> The value type.
     * @return The list of components.
     */
    @SuppressWarnings("unchecked")
    public <T extends NodeComponent> Subset<T> findComponents(Class<T> kl) {
        return (Subset<T>) Subset.filter(components, c -> kl.isAssignableFrom(c.getClass()));
    }

    /**
     * Attach the given component to this node, this is the
     * only mutable part of the node.
     *
     * @param component The component.
     * @return This.
     */
    public ManagedNode attach(NodeComponent component) {
        if (component == null)
            return this;

        if (component.attached(this)) {
            components.add(component);
        }

        return this;
    }

    // run the action through the components
    @SuppressWarnings("unchecked")
    private <T, C extends NodeComponent> CompletableFuture<T> runAction(Class<C> component, BiFunction<C, ManagedNode, ?> function, Function<Throwable, T> tFunction) {
        CompletableFuture<Void>[] futures = (CompletableFuture<Void>[]) findComponents(component)
                .stream()
                .map(c -> function.apply(c, this))
                .toArray();

        CompletableFuture<T> future = new CompletableFuture<>();
        CompletableFuture.allOf(futures).whenComplete((__, t) -> future.complete(tFunction.apply(t)));
        return future;
    }

    /**
     * Creates this node.
     *
     * @return The creation result future.
     */
    public CompletableFuture<CreationResult> create() {
        return this.runAction(NodeCreateAdapter.class, NodeCreateAdapter::create,
                t -> t == null ? new CreationResult(false, null) : new CreationResult(true, Collections.singletonList(t)));
    }

}
