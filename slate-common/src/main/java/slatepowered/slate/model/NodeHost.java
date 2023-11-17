package slatepowered.slate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import slatepowered.slate.allocation.NodeAllocator;

/**
 * Declares a node as the host for another node.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NodeHost implements SharedNodeComponent {

    public static NodeHost by(Node node) {
        return new NodeHost(node);
    }

    @Override
    public boolean attached(ManagedNode node) {
        // register NodeAllocator service as a component
        // if it exists on the node host
        NodeAllocator allocator = host.getService(NodeAllocator.KEY);
        if (allocator != null) {
            node.attach(allocator);
        }

        return true;
    }

    /**
     * The host (cluster) of this node.
     */
    protected Node host;

}
