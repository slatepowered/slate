package slatepowered.slate.model;

import slatepowered.slate.logging.Logging;
import slatepowered.slate.model.action.InitializationResult;
import slatepowered.slate.model.action.NodeInitializeAdapter;
import slatepowered.slate.model.action.NodeDestroyAdapter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Represents a node you have management capabilities over.
 */
@SuppressWarnings("rawtypes")
public abstract class ManagedNode extends Node {

    private static final Logger LOGGER = Logging.getLogger("ManagedNode");

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

    @SuppressWarnings("unchecked")
    public ManagedNode(Node parent, String name, Network network, List<NodeComponent> components) {
        super(name, network);
        this.parent = parent;
        this.components = components;
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
    public <T extends NodeComponent> NavigableSet<T> findComponents(Class<T> kl) {
        NavigableSet<T> set = new TreeSet<>();
        for (NodeComponent component : components) {
            if (kl.isAssignableFrom(component.getClass())) {
                set.add((T) component);
            }
        }

        return set;
    }

    /**
     * @see ManagedNode#findComponents(Class)
     */
    @SuppressWarnings("unchecked")
    public <T extends NodeComponent> List<T> listComponents(Class<T> kl) {
        List<T> set = new ArrayList<>();
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

}
