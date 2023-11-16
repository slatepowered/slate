package slatepowered.slate.cluster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import slatepowered.slate.allocation.ClusterAllocationChecker;
import slatepowered.slate.allocation.LocalNodeAllocation;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.plugin.SlatePluginManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Locally manages clusters for any network which requests it,
 * and provides shared data like local packages to be used by
 * any network.
 */
@Getter
@RequiredArgsConstructor
public abstract class Cluster<I extends ClusterInstance> {

    /**
     * The name of this cluster.
     */
    protected final String name;

    /**
     * All cluster instances by communication key.
     */
    protected final Map<CommunicationKey, I> instanceMap = new HashMap<>();

    /**
     * All local node allocations.
     */
    protected final List<LocalNodeAllocation> localAllocations = new ArrayList<>();

    /**
     * The local plugin manager.
     */
    protected final SlatePluginManager pluginManager = new SlatePluginManager() {
        final String[] envNames = new String[] { "nodehost", "host", "cluster" };

        @Override
        public String[] getEnvironmentNames() {
            return envNames;
        }
    };

    /**
     * Get the local package manager for this cluster.
     *
     * @return The package manager.
     */
    public abstract PackageManager getLocalPackageManager();

    /**
     * Get the plugin manager on this cluster.
     *
     * @return The plugin manager instance.
     */
    public SlatePluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * Start this cluster' operations.
     */
    public abstract void start();

    /**
     * Close this cluster.
     */
    public abstract void close();

    /**
     * Get a cluster instance for the given communication key.
     *
     * @param key The communication key.
     * @return The instance.
     */
    public abstract I createInstance(CommunicationKey key);

    public I getOrCreateInstance(CommunicationKey key) {
        I instance = getInstance(key);
        if (instance == null) {
            instanceMap.put(key, createInstance(key));
        }

        return instance;
    }

    public I getInstance(CommunicationKey key) {
        return instanceMap.get(key);
    }

    public void closeInstance(ClusterInstance instance) {
        instanceMap.remove(instance.getCommunicationKey());
    }

    /**
     * Get the path to the instance directory for the given instance.
     *
     * @param instance The instance.
     * @return The directory path.
     */
    public abstract Path getInstanceDirectory(ClusterInstance instance);

    public List<LocalNodeAllocation> getLocalAllocations() {
        return localAllocations;
    }

}
