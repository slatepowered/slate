package slatepowered.slate.master;

import slatepowered.slate.cluster.IntegratedCluster;
import slatepowered.slate.cluster.IntegratedClusterInstance;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;
import slatepowered.slate.model.MasterManagedNode;
import slatepowered.slate.model.MasterNetwork;
import slatepowered.slate.network.NetworkInfoService;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.plugin.SlatePluginManager;
import slatepowered.veru.data.Pair;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * The master bootstrap/network.
 */
public class Master extends MasterNetwork {

    /**
     * The working directory.
     */
    protected final Path directory;

    /**
     * The local package manager for locally managing packages.
     */
    protected final PackageManager localPackageManager;

    /**
     * The {@link slatepowered.slate.cluster.Cluster} impl of the integrated cluster.
     */
    protected final IntegratedCluster integratedClusterImpl;

    /**
     * The integrated cluster instance.
     */
    protected final IntegratedClusterInstance integratedClusterInstance;

    /**
     * The plugin manager.
     */
    protected final SlatePluginManager pluginManager;

    Master(Path directory, CommunicationKey communicationKey, CommunicationStrategy communicationStrategy) {
        super(communicationKey, communicationStrategy);
        this.directory = directory;

        localPackageManager = new PackageManager(directory.resolve("packages"));

        this.pluginManager = new SlatePluginManager() {
            final String[] envNames = new String[] { "master", "controller" };

            @Override
            public String[] getEnvironmentNames() {
                return envNames;
            }
        };

        integratedClusterImpl = new IntegratedCluster("master.integrated-cluster", this);
        integratedClusterInstance = new IntegratedClusterInstance(integratedClusterImpl, communicationKey, communicationStrategy);
    }

    public SlatePluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * Get the working/data directory for this master controller.
     *
     * @return The directory path.
     */
    public Path getDirectory() {
        return directory;
    }

    /**
     * Get the integrated cluster instance for configuration.
     *
     * @return The integrated cluster instance.
     */
    public IntegratedClusterInstance getIntegratedCluster() {
        return integratedClusterInstance;
    }

    /**
     * Get the local package manager utilized by this master node.
     *
     * @return The package manager.
     */
    public PackageManager getLocalPackageManager() {
        return localPackageManager;
    }

    /**
     * Closes the network, this should destroy all services.
     */
    public void close() {
        rpcManager.invokeRemoteEvent(NetworkInfoService.class, "onClose", null);

        getIntegratedCluster().close();
        getIntegratedCluster().getCluster().close();

        pluginManager.disable(this);
        pluginManager.destroy();

        // finally, close the communication provider
        // this basically closes the network until
        // a whole new network instance is created
        communicationProvider.close();
    }

    @Override
    public void destroy() {
        close();
    }

    /* ----------- Register Services ----------- */

    {
        serviceManager.register(NetworkInfoService.key(), new NetworkInfoService() {
            @Override
            public Collection<Pair<String, String[]>> fetchNodeNames() {
                return Master.this.getNodeMap().values().stream()
                        .map(n -> Pair.of(n.getName(), n.getTags()))
                        .collect(Collectors.toList());
            }

            @Override
            public NodeInfo fetchNodeInfo(String name) {
                MasterManagedNode node = Master.this.getNode(name);
                return new NodeInfo(node.getName(), node.getParent().getName(), node.getTags());
            }
        });
    }

    /**
     * Builds a network master on this JVM.
     */
    public static class MasterBuilder {

        private Path directory = Paths.get("./");
        private CommunicationKey communicationKey;
        private CommunicationStrategy communicationStrategy;

        public MasterBuilder directory(Path directory) {
            this.directory = directory;
            return this;
        }

        public MasterBuilder communicationKey(CommunicationKey communicationKey) {
            this.communicationKey = communicationKey;
            return this;
        }

        public MasterBuilder communicationStrategy(CommunicationStrategy communicationStrategy) {
            this.communicationStrategy = communicationStrategy;
            return this;
        }

        public Master build() {
            return new Master(directory, communicationKey, communicationStrategy);
        }

    }

    public static MasterBuilder builder() {
        return new MasterBuilder();
    }

}
