package slatepowered.slate.cluster;

import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.ProvidedChannel;
import slatepowered.slate.allocation.*;
import slatepowered.slate.model.ClusterManagedNode;
import slatepowered.slate.model.ClusterNetwork;
import slatepowered.slate.model.Node;
import slatepowered.slate.model.NodeComponent;
import slatepowered.slate.model.action.NodeAllocationAdapter;
import slatepowered.slate.model.action.NodeInitializeAdapter;
import slatepowered.slate.model.services.NetworkInfoService;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageAttachment;
import slatepowered.slate.packages.PackageManager;
import slatepowered.veru.misc.Throwables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this cluster for a specific network.
 */
public class ClusterInstance extends ClusterNetwork {

    /**
     * The cluster.
     */
    private final Cluster cluster;

    /**
     * The directory for this cluster instance.
     */
    private final Path directory;

    public ClusterInstance(Cluster cluster, CommunicationProvider<? extends ProvidedChannel> communicationProvider) {
        super(communicationProvider);
        this.cluster = cluster;
        this.directory = cluster.getDirectory().resolve("instances").resolve(String.valueOf(System.currentTimeMillis() ^ System.nanoTime()));

        // register the cluster services
        serviceManager.register(PackageManager.KEY, cluster.getLocalPackageManager());

        try {
            Files.createDirectories(directory);
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
        }
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

    {
        // register the remote node allocation service
        serviceManager.register(NodeAllocator.KEY, new NodeAllocator() {
            @Override
            public Boolean canAllocate(String parent, String[] tags) {
                return cluster.getAllocationChecker().canAllocate(cluster, ClusterInstance.this, parent, tags);
            }

            @Override
            @SuppressWarnings("unchecked")
            public NodeAllocationResult allocate(NodeAllocationRequest request) {
                try {
                    NetworkInfoService networkInfoService = getService(NetworkInfoService.KEY);

                    // create node locally
                    ClusterManagedNode node = new ClusterManagedNode(
                            fetchAndCreateNode(request.getParentNodeName()),
                            request.getNodeName(),
                            ClusterInstance.this,
                            (List<NodeComponent>)(Object)request.getComponents(),
                            request.getTags()
                    ) { };

                    // create allocation
                    LocalNodeAllocation localNodeAllocation = new LocalNodeAllocation(node, directory.resolve("nodes").resolve(node.getName()));
                    Files.createDirectories(localNodeAllocation.getDirectory());

                    // install packages
                    PackageManager packageManager = cluster.getPackageManager();
                    node.findComponents(PackageAttachment.class).forEach(packageAttachment -> {
                        packageManager.findOrInstallPackage(packageAttachment.getSourcePackage()).whenComplete((localPackage, throwable) -> {
                            packageAttachment.install(packageManager, node, localNodeAllocation.getDirectory(), localPackage);
                        });
                    });

                    // execute other allocation components
                    node.findComponents(NodeAllocationAdapter.class).forEach(adapter ->
                            adapter.initialize(packageManager, node, localNodeAllocation.getDirectory()));

                    return new NodeAllocation(node.getName(), /* todo: components to add */ new ArrayList<>());
                } catch (Throwable e) {
                    return new FailedNodeAllocation(e);
                }
            }

            @Override
            public void destroy(String name) {
                try {
                    // find node
                    Node n = getNode(name);
                    if (!(n instanceof ClusterManagedNode))
                        throw new IllegalStateException("Node `" + name + "` is not managed by this cluster");

                    ClusterManagedNode node = (ClusterManagedNode) n;
                    LocalNodeAllocation allocation = node.getAllocation();

                    // destroy files
                    Files.deleteIfExists(allocation.getDirectory());

                    // unregister node
                    nodeMap.remove(name);
                } catch (Throwable e) {
                    Throwables.sneakyThrow(e);
                }
            }
        });
    }

    /**
     * Builds a {@link ClusterInstance}.
     */
    public static class ClusterInstanceBuilder {

        private final Cluster cluster;
        private CommunicationProvider<?> communicationProvider;

        public ClusterInstanceBuilder(Cluster cluster) {
            this.cluster = cluster;
        }

        public ClusterInstanceBuilder communicationProvider(CommunicationProvider<?> communicationProvider) {
            this.communicationProvider = communicationProvider;
            return this;
        }

        public ClusterInstance build() {
            return new ClusterInstance(cluster, communicationProvider);
        }

    }

    public static ClusterInstanceBuilder builder(Cluster cluster) {
        return new ClusterInstanceBuilder(cluster);
    }

}
