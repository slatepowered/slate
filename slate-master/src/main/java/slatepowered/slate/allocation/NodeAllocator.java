package slatepowered.slate.allocation;

import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.model.MasterManagedNode;
import slatepowered.slate.model.NodeComponent;
import slatepowered.slate.model.SharedNodeComponent;
import slatepowered.slate.model.action.NodeCreateAdapter;
import slatepowered.veru.misc.Throwables;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Determines how a node is allocated and created on a cluster
 * or other type of host.
 */
public interface NodeAllocator extends NodeCreateAdapter, RemoteAPI {

    Logger LOGGER = Logger.getLogger("NodeAllocator");

    /**
     * Checks whether this allocator has the resources available
     * to allocate another node.
     *
     * @return Whether it could allocate a node.
     */
    Boolean canAllocate(String parent, String[] tags);

    CompletableFuture<Boolean> canAllocateAsync(String parent, String[] tags);

    /**
     * Attempts to allocate the node with the given data on
     * this node allocator.
     *
     * @param request The request.
     * @return The result of the allocation.
     */
    NodeAllocationResult allocate(NodeAllocationRequest request);

    CompletableFuture<NodeAllocationResult> allocateAsync(NodeAllocationRequest request);

    @Override
    default CompletableFuture<Void> create(ManagedNode node) {
        if (!(node instanceof MasterManagedNode))
            throw new IllegalArgumentException("Can only make allocation requests with master managed nodes");
        MasterManagedNode mNode = (MasterManagedNode) node;

        final List<SharedNodeComponent> sharedNodeComponents = node.findComponents(SharedNodeComponent.class).collect();
        final NodeAllocationRequest allocationRequest = new NodeAllocationRequest(mNode.getParent().getName(), node.getName(), node.getTags(), sharedNodeComponents);

        return canAllocateAsync(mNode.getParent().getName(), mNode.getTags())
                .thenApply(b -> {
                    if (b) {
                        // allocate node and add result as component
                        allocateAsync(allocationRequest).whenComplete((result, t) -> {
                            if (t != null) {
                                Throwables.sneakyThrow(t);
                                return;
                            }

                            if (!result.isSuccessful()) {
                                // failed to allocate
                                LOGGER.warning("Failed to allocate node `" + node.getName() + "`");
                                if (result.failed().getError() != null) {
                                    result.failed().getError().printStackTrace();
                                }

                                return;
                            }

                            // handle successful allocation
                            NodeAllocation allocation = result.successful();
                            node.attach(allocation);
                            for (NodeComponent component : allocation.getComponents()) {
                                node.attach(component);
                            }
                        });
                    }
                });
    }
}
