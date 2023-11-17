package slatepowered.slate.cluster;

import slatepowered.slate.allocation.*;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;
import slatepowered.slate.logging.Logger;
import slatepowered.slate.logging.Logging;
import slatepowered.slate.model.ClusterManagedNode;
import slatepowered.slate.model.ClusterNetwork;
import slatepowered.slate.model.Node;
import slatepowered.slate.model.NodeComponent;
import slatepowered.slate.action.NodeAllocationAdapter;
import slatepowered.slate.network.NetworkInfoService;
import slatepowered.slate.packages.PackageAttachment;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.Packages;
import slatepowered.slate.packages.service.LateAttachmentService;
import slatepowered.slate.plugin.SlatePluginManager;
import slatepowered.veru.collection.Sequence;
import slatepowered.veru.misc.Throwables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * An instance of this cluster for a specific network.
 */
public abstract class ClusterInstance extends ClusterNetwork {

    protected static final Logger LOGGER = Logging.getLogger("ClusterInstance");

    /**
     * The cluster.
     */
    protected final Cluster<?> cluster;

    /**
     * The directory for this cluster instance.
     */
    protected final Path directory;

    /**
     * Whether this instance is enabled.
     */
    private boolean enabled = true;

    public ClusterInstance(Cluster<?> cluster, CommunicationKey communicationKey, CommunicationStrategy communicationStrategy) {
        super(communicationKey, communicationStrategy);
        this.cluster = cluster;
        this.directory = cluster.getInstanceDirectory(this);

        cluster.getPluginManager().initialize(this);

        try {
            Files.createDirectories(directory);
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
        }
    }

    /**
     * Set if this cluster instance is enabled.
     *
     * When disabled a cluster instance will not accept allocation
     * or destruction of nodes, essentially sitting idle.
     *
     * @param enabled Enable flag.
     * @return This.
     */
    public ClusterInstance setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Cluster getCluster() {
        return cluster;
    }

    // first check registered nodes, otherwise
    // fetch the info about a node remotely
    // and create it as a non-managed node
    private Node fetchAndCreateNode(String name) {
        Node node = getNode(name);
        if (node == null) {
            NetworkInfoService.NodeInfo nodeInfo = getService(NetworkInfoService.KEY)
                    .fetchNodeInfo(name);

            node = new Node(nodeInfo.getName(), this) {
                @Override
                public String[] getTags() {
                    return nodeInfo.getTags();
                }
            };
        }

        return node;
    }

    /**
     * Closes this cluster instance.
     */
    public void close() {
        cluster.getPluginManager().disable(this);
        cluster.closeInstance(this);
    }

    /**
     * Get the allocation checker to check availability.
     *
     * @return The allocation checker.
     */
    protected ClusterAllocationChecker getAllocationChecker() {
        return null;
    }

    /**
     * Attempts to allocate and initialize a node following the given request.
     *
     * @param request The allocation request.
     * @return The allocation result.
     */
    @SuppressWarnings("unchecked")
    public ClusterManagedNode allocateAndInitializeNode(NodeAllocationRequest request) {
        try {
            NetworkInfoService networkInfoService = getService(NetworkInfoService.KEY);

            // create node locally
            LOGGER.debug("Allocating node with name(" + request.getNodeName() + ") with componentCount(" + request.getComponents().size() + ")");
            ClusterManagedNode node = new ClusterManagedNode(
                    fetchAndCreateNode(request.getParentNodeName()),
                    request.getNodeName(),
                    ClusterInstance.this,
                    (List<NodeComponent>)(Object)request.getComponents(),
                    request.getTags()
            ) { };

            // create allocation
            LocalNodeAllocation localNodeAllocation = new LocalNodeAllocation(node, directory.resolve("nodes").resolve(node.getName()));
            cluster.localAllocations.add(localNodeAllocation);
            Files.createDirectories(localNodeAllocation.getDirectory());
            LOGGER.debug("Created allocation for node(" + node.getName() + ") in directory(" + directory + ")");

            // install packages
            PackageManager packageManager = cluster.getLocalPackageManager();
            Packages.attachAll(
                    packageManager,
                    node.findComponents(PackageAttachment.class),
                    node,
                    localNodeAllocation.getDirectory(),
                    local(),
                    directory
            ).whenComplete((throwables, __) -> {
                throwables.forEach(Throwable::printStackTrace);
            });

            // execute other allocation components
            Sequence<NodeAllocationAdapter> adapters = node.findComponents(NodeAllocationAdapter.class);
            LOGGER.debug("Execute allocation of node(name: " + node.getName() + ") for adapter count(" + adapters.size() + ")");
            node.findComponents(NodeAllocationAdapter.class).forEach(adapter ->
                    adapter.initialize(packageManager, node, localNodeAllocation.getDirectory()));

            return node;
        } catch (Throwable e) {
            Throwables.sneakyThrow(e);
            throw new AssertionError();
        }
    }

    /**
     * Locally destroys the given node on this cluster.
     *
     * @param node The node to destroy.
     */
    public void destroyNode(ClusterManagedNode node) {
        try {
            LOGGER.debug("Destroying node(name: " + node.getName() + ") on this cluster");
            LocalNodeAllocation allocation = node.getAllocation();
            cluster.localAllocations.remove(allocation);
            allocation.destroy();

            // unregister node
            nodeMap.remove(node.getName());
        } catch (Throwable e) {
            Throwables.sneakyThrow(e);
        }
    }

    {
        /* ----------- Register Services ----------- */

        /*
            Node allocation and initialization
         */
        serviceManager.register(NodeAllocator.KEY, new NodeAllocator() {
            @Override
            public Boolean canAllocate(String name, String parent, String[] tags) {
                if (!isEnabled()) return false;
                LOGGER.debug("Checking for allocation possibility here on cluster(name: " + getCluster().getName() + ")");
                return getAllocationChecker().canAllocate(cluster, ClusterInstance.this, name, parent, tags);
            }

            @Override
            public NodeAllocationResult allocate(NodeAllocationRequest request) {
                if (!isEnabled()) return new FailedNodeAllocation(new IllegalStateException("Cluster instance is not enabled"));

                try {
                    ClusterManagedNode node = allocateAndInitializeNode(request);
                    return new NodeAllocation(node.getName(), /* todo: components to register */ new ArrayList<>());
                } catch (Throwable t) {
                    return new FailedNodeAllocation(t);
                }
            }

            @Override
            public void destroy(String name) {
                if (!isEnabled()) return;

                Node n = getNode(name);
                if (!(n instanceof ClusterManagedNode))
                    throw new IllegalStateException("Node `" + name + "` is not managed by this cluster");

                destroyNode((ClusterManagedNode) n);
            }
        });

        /*
            Late package attachment
         */
        serviceManager.register(LateAttachmentService.KEY, new LateAttachmentService() {
            @Override
            public void attachImmediate(List<PackageAttachment<?>> attachments) {
                Packages.attachAll(
                        cluster.getLocalPackageManager(),
                        attachments,
                        local(),
                        directory,
                        local(),
                        directory
                ).whenComplete((throwables, __) -> {
                    throwables.forEach(Throwable::printStackTrace);
                });
            }

            @Override
            public CompletableFuture<Void> attachImmediateAsync(List<PackageAttachment<?>> attachments) {
                attachImmediate(attachments);
                return CompletableFuture.completedFuture(null);
            }
        });
    }

}
