package slatepowered.slate.model;

import slatepowered.slate.logging.Logging;
import slatepowered.veru.collection.Sequence;

import java.util.*;
import java.util.logging.Logger;

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
    public ManagedNode attach(NodeComponent component) {
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

}
