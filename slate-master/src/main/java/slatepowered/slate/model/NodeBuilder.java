package slatepowered.slate.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds managed node instances on the network.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class NodeBuilder {

    /**
     * The parent node of the resulting node.
     */
    protected final MasterManagedNode parent;

    /**
     * The name of the resulting node.
     */
    protected final String name;

    /**
     * The components to set on the node.
     */
    protected final List<NodeComponent> components = new ArrayList<>();

    /**
     * The tags on this node.
     */
    protected String[] tags;

    public NodeBuilder tags(String[] tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Attaches the given component to the node that is being built.
     *
     * @param component The component.
     * @return The node builder.
     */
    public NodeBuilder attach(NodeComponent component) {
        components.add(component);
        return this;
    }

    /**
     * Builds the node and registers it to the network.
     *
     * @return The built node.
     */
    public MasterManagedNode build() {
        MasterManagedNode node = new MasterManagedNode(parent, name, parent.getNetwork(), components) {
            {
                // register this node to the network
                network.registerNode(this);

                for (NodeComponent component : this.components) {
                    attach(component);
                }
            }

            /* --------------------------------------------- */

            @Override
            public String[] getTags() {
                return tags;
            }
        };

        // adopt and return the node
        parent.adopt(node);
        return node;
    }

}
