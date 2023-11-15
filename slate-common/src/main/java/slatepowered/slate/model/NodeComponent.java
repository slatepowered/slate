package slatepowered.slate.model;

import java.util.function.Consumer;
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
     * Runs the given consumer when this component is attached,
     * but doesn't attach the component itself essentially making
     * it a hook into the attachment event.
     *
     * @param action The action to run.
     * @param <N> The node type.
     * @return The component.
     */
    static <N extends ManagedNode> NodeComponent attachHook(Consumer<N> action) {
        return new NodeComponent() {
            @Override
            @SuppressWarnings("unchecked")
            public boolean attached(ManagedNode node) {
                action.accept((N) node);
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
