package slatepowered.slate.allocation;

import slatepowered.reco.rpc.Local;
import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.reco.rpc.function.Allow;
import slatepowered.slate.logging.Logger;
import slatepowered.slate.logging.Logging;
import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.model.NodeComponent;
import slatepowered.slate.model.SharedNodeComponent;
import slatepowered.slate.action.NodeInitializeAdapter;
import slatepowered.slate.action.NodeDestroyAdapter;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceKey;
import slatepowered.slate.service.remote.LocalRemoteServiceKey;
import slatepowered.veru.misc.Throwables;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Determines how a node is allocated and created on a cluster
 * or other type of host.
 */
public interface NodeAllocator extends NodeInitializeAdapter, NodeDestroyAdapter, RemoteAPI, Service {

    ServiceKey<NodeAllocator> KEY = LocalRemoteServiceKey.key(NodeAllocator.class);

    Logger LOGGER = Logging.getLogger("NodeAllocator");

    /**
     * Checks whether this allocator has the resources available
     * to allocate another node.
     *
     * @return Whether it could allocate a node.
     */
    @Allow("all")
    Boolean canAllocate(String name, String parent, String[] tags);

    default CompletableFuture<Boolean> canAllocateAsync(String name, String parent, String[] tags) {
        return CompletableFuture.completedFuture(canAllocate(name, parent, tags));
    }

    /**
     * Attempts to allocate/create the node with the given data on
     * this node allocator.
     *
     * @param request The request.
     * @return The result of the allocation.
     */
    @Allow("master")
    NodeAllocationResult allocate(NodeAllocationRequest request);

    default CompletableFuture<NodeAllocationResult> allocateAsync(NodeAllocationRequest request) {
        return CompletableFuture.supplyAsync(() -> allocate(request));
    }

    /**
     * Destroy the node by the given name on this allocator.
     *
     * @param name The name of the node to destroy.
     */
    void destroy(String name);

    default CompletableFuture<Void> destroyAsync(String name) {
        return null;
    }

    @Override
    @Local
    default CompletableFuture<Void> create(ManagedNode node) {
        final List<SharedNodeComponent> sharedNodeComponents = new ArrayList<>(node.findComponents(SharedNodeComponent.class));
        final NodeAllocationRequest allocationRequest = new NodeAllocationRequest(node.getParent().getName(), node.getName(), node.getTags(), sharedNodeComponents);

        return canAllocateAsync(node.getName(), node.getParent().getName(), node.getTags())
                .thenCompose(b -> {
                    if (b) {
                        LOGGER.debug("Trying to allocate node(name: " + node.getName() + ") with(" + sharedNodeComponents.size() + " shared components)");

                        // allocate node and add result as component
                        return allocateAsync(allocationRequest).whenComplete((result, t) -> {
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
                            LOGGER.debug("Successfully allocated node(name: " + node.getName() + ") attaching(" + allocation.getComponents().size() + " components)");
                            node.attach(allocation);
                            for (NodeComponent component : allocation.getComponents()) {
                                node.attach(component);
                            }
                        }).<Void>thenApply(__ -> null);
                    }

                    return CompletableFuture.completedFuture(null);
                });
    }

    @Override
    @Local
    default CompletableFuture<Void> destroy(ManagedNode node) {
        return destroyAsync(node.getName());
    }

}
