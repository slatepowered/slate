package slatepowered.slate.model.action;

import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.model.NodeComponent;

import java.util.concurrent.CompletableFuture;

/**
 * A component which handles destruction of a component.
 */
public interface NodeDestroyAdapter extends NodeComponent {

    CompletableFuture<Void> destroy(ManagedNode node);

}
