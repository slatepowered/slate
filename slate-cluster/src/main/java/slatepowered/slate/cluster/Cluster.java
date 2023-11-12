package slatepowered.slate.cluster;

import lombok.Builder;
import lombok.Getter;
import slatepowered.slate.allocation.ClusterAllocationChecker;
import slatepowered.slate.allocation.LocalNodeAllocation;
import slatepowered.slate.packages.PackageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Locally manages clusters for any network which requests it,
 * and provides shared data like local packages to be used by
 * any network.
 */
@Getter
@Builder
public class Cluster {

    /**
     * The local package manager.
     */
    private final PackageManager packageManager;

    /**
     * The cluster instances.
     */
    private final List<ClusterInstance> instances = new ArrayList<>();

    /**
     * All local node allocations.
     */
    private final List<LocalNodeAllocation> allocations = new ArrayList<>();

    /**
     * The allocation checker.
     */
    private final ClusterAllocationChecker allocationChecker;

    /**
     * Get the local package manager.
     *
     * @return The manager.
     */
    public PackageManager getLocalPackageManager() {
        return packageManager;
    }

    /**
     * Get the instances managed by this cluster.
     *
     * @return The list of instances.
     */
    public List<ClusterInstance> getInstances() {
        return instances;
    }

    public ClusterInstance.ClusterInstanceBuilder instance() {
        return ClusterInstance.builder(this);
    }

    public ClusterAllocationChecker getAllocationChecker() {
        return allocationChecker;
    }

    public List<LocalNodeAllocation> getAllocations() {
        return allocations;
    }

}
