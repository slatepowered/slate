package slatepowered.slate.model;

import slatepowered.slate.logging.Logger;
import slatepowered.slate.logging.Logging;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceKey;
import slatepowered.slate.service.network.NodeHostBoundServiceKey;
import slatepowered.veru.collection.Sequence;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a node you have management capabilities over.
 */
@SuppressWarnings("rawtypes")
public abstract class ManagedNode extends Node {

    protected static final Logger LOGGER = Logging.getLogger("ManagedNode");

    /**
     * The children of this node.
     */
    protected final Map<String, ManagedNode> children = new HashMap<>();

    /**
     * The parent node.
     */
    protected final Node parent;

    /**
     * All components attached to this node.
     */
    protected final List<NodeComponent> components;

    public ManagedNode(Node parent, String name, Network network, List<NodeComponent> components) {
        super(name, network);
        this.parent = parent;
        this.components = components;
    }

    public ManagedNode(Node parent, String name, Network network) {
        super(name, network);
        this.parent = parent;
        this.components = new ArrayList<>();
    }

    public Node getParent() {
        return parent;
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
    public <T extends NodeComponent> Sequence<T> findComponents(Class<? super T> kl) {
        Sequence<T> set = new Sequence<>();
        for (NodeComponent component : components) {
            if (kl.isAssignableFrom(component.getClass())) {
                set.add((T) component);
            }
        }

        return set;
    }

    /**
     * Attach the given component to this node, this is the
     * only mutable part of the node.
     *
     * @param component The component.
     * @return This.
     */
    public synchronized ManagedNode attach(NodeComponent component) {
        if (component == null)
            return this;

        if (component.attached(this)) {
            components.add(component);
        }

        return this;
    }

    public Map<String, ManagedNode> getChildren() {
        return children;
    }

    public ManagedNode getChild(String name) {
        return children.get(name);
    }

    /**
     * Assume this operation is valid under normal circumstances
     * and blindly adopt the given node as this node's child.
     *
     * @param node The node to adopt.
     * @return This.
     */
    public ManagedNode adopt(ManagedNode node) {
        children.put(node.getName(), node);
        return this;
    }

    /**
     * Execute an action through all the components
     *
     * @param componentClass The component class to match the components against.
     * @param invoker The invoker function which runs the action and returns the result.
     * @param resultComposer The result composer which composes any errors into a result object.
     * @param <T> The result type.
     * @param <C> The component type.
     * @return The result future.
     */
    @SuppressWarnings("unchecked")
    public <T, C extends NodeComponent> CompletableFuture<T> runVoidAction(Class<C> componentClass, BiFunction<C, ManagedNode, CompletableFuture<?>> invoker, Function<Throwable, T> resultComposer) {
        List<CompletableFuture<?>> futureList = new ArrayList<>();
        for (C component : findComponents(componentClass)) {
            LOGGER.info("  action executed on " + component);
            futureList.add(invoker.apply(component, this));
        }

        if (resultComposer != null) {
            return CompletableFuture
                    .allOf(futureList.toArray(new CompletableFuture[0]))
                    .thenApply(__ -> resultComposer.apply(null))
                    .exceptionally(resultComposer);
        } else {
            return (CompletableFuture<T>) CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        }
    }

    @Override
    public <T extends Service> ServiceKey<T> qualifyServiceKey(ServiceKey<T> key) throws UnsupportedOperationException {
        if (key instanceof NodeHostBoundServiceKey) {
            ((NodeHostBoundServiceKey) key).forNode(
                    this.findComponents(NodeHost.class).first().map(h -> h.host.getName()).orElse(null));
        }

        return super.qualifyServiceKey(key);
    }

}
