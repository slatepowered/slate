package slatepowered.slate.allocation;

import slatepowered.slate.model.ClusterManagedNode;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents the allocation of a node on a cluster.
 */
public class LocalNodeAllocation {

    /**
     * The network node this allocation is for.
     */
    private final ClusterManagedNode node;

    /**
     * The network node directory.
     */
    private final Path directory;

    public LocalNodeAllocation(ClusterManagedNode node, Path directory) {
        this.node = node;
        this.directory = directory;
    }

    public ClusterManagedNode getNode() {
        return node;
    }

    public Path getDirectory() {
        return directory;
    }

    /**
     * Destroys this node allocation.
     */
    public void destroy() {
        try {
            // destroy files
            Files.delete(directory);
        } catch (Throwable t) {
            throw new IllegalStateException("Failed to destroy node `" + node.getName() + "`", t);
        }
    }

}
