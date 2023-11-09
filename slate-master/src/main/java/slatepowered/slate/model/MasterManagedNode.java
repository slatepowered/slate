package slatepowered.slate.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ManagedNode} on the master instance, with
 * full control permissions.
 */
public abstract class MasterManagedNode extends ManagedNode {

    /**
     * The parent node object of this node.
     */
    protected final MasterManagedNode parent;

    /**
     * The children of this node.
     */
    protected final Map<String, MasterManagedNode> children = new HashMap<>();

    public MasterManagedNode(MasterManagedNode parent, String name, Network<Node> network, List<NodeComponent> components) {
        super(name, network, components);
        this.parent = parent;
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
     * Assume this operation is valid under normal circumstances
     * and blindly adopt the given node as this node's child.
     *
     * @param node The node to adopt.
     * @return This.
     */
    public MasterManagedNode adopt(MasterManagedNode node) {
        children.put(node.getName(), node);
        return this;
    }

    public Map<String, MasterManagedNode> getChildren() {
        return children;
    }

    public MasterManagedNode getChild(String name) {
        return children.get(name);
    }

    public MasterManagedNode getParent() {
        return parent;
    }

}
