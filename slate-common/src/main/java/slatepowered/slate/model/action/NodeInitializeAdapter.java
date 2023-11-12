package slatepowered.slate.model.action;

import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.model.NodeComponent;

import java.util.concurrent.CompletableFuture;

/**
 * A component which handles creation of a component.
 */
public interface NodeInitializeAdapter extends NodeComponent {

    CompletableFuture<Void> create(ManagedNode node);

}
