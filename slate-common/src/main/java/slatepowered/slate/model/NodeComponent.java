package slatepowered.slate.model;

import java.util.function.Function;

/**
 * A component attached to a node on the network which
 * determines parts of it's behaviour/data.
 */
public interface NodeComponent {

    static NodeComponent attachOther(Function<ManagedNode, NodeComponent> component) {
        return new NodeComponent() {
            @Override
            public boolean attached(ManagedNode node) {
                node.attach(component.apply(node));
                return false;
            }
        };
    }

    /**
     * Called when this component is attached to the given node.
     *
     * @param node The managed node.
     * @return Whether this component should be attached to the given node.
     */
    default boolean attached(ManagedNode node) {
        return true;
    }

}
