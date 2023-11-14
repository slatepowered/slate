package slatepowered.slate.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

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
    protected final List<NodeComponent> components = new Vector<>();

    /**
     * The tags on this node.
     */
    protected String[] tags;

    /**
     * The channel name override.
     */
    protected String channelName;

    public String getName() {
        return name;
    }

    public MasterManagedNode getParent() {
        return parent;
    }

    public MasterNetwork getNetwork() {
        return parent.getNetwork();
    }

    /**
     * Set the tags for this node.
     *
     * @param tags The tags.
     * @return This.
     */
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
     * Set the communication channel name override for this node.
     *
     * @param channelName The channel name.
     * @return This.
     */
    public NodeBuilder channelName(String channelName) {
        this.channelName = channelName;
        return this;
    }

    /**
     * Builds the node and registers it to the network.
     *
     * @return The built node.
     */
    public MasterManagedNode build() {
        MasterManagedNode node = new MasterManagedNode(parent, name, parent.getNetwork(), new Vector<>()) {
            {
                // register this node to the network
                network.registerNode(this);

                for (NodeComponent component : NodeBuilder.this.components) {
                    attach(component);
                }
            }

            /* --------------------------------------------- */

            @Override
            public String[] getTags() {
                return tags;
            }

            @Override
            public String remoteChannelName() {
                return channelName != null ? channelName : super.remoteChannelName();
            }
        };

        // adopt and return the node
        parent.adopt(node);
        return node;
    }

}
