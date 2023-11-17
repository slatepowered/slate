package slatepowered.slate.cluster;

import slatepowered.slate.allocation.ClusterAllocationChecker;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;
import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.model.Node;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.plugin.SlatePluginManager;

import java.util.ArrayList;
import java.util.Map;

/**
 * An instance of a {@link DedicatedCluster} dedicated to one network.
 */
public class DedicatedClusterInstance extends ClusterInstance {

    // The local node
    private final ManagedNode localNode;

    public DedicatedClusterInstance(Cluster<?> cluster, CommunicationKey communicationKey, CommunicationStrategy communicationStrategy) {
        super(cluster, communicationKey, communicationStrategy);

        register(PackageManager.KEY, cluster.getLocalPackageManager());
        register(SlatePluginManager.KEY, cluster.getPluginManager());

        this.localNode = new ManagedNode(master(), cluster.getName(), this) {
            final String[] tags = new String[] { "*", "cluster", "local" };

            @Override
            public String[] getTags() {
                return tags;
            }
        };
    }

    @Override
    protected ClusterAllocationChecker getAllocationChecker() {
        return ((DedicatedCluster)cluster).getAllocationChecker();
    }

    @Override
    public ManagedNode local() {
        return localNode;
    }

}
