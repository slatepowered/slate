package slatepowered.slate.cluster;

import slatepowered.slate.model.NodeBuilder;
import slatepowered.slate.model.NodeBuilderAdapter;

/**
 * Helper for working with building cluster nodes.
 */
public final class ClusterNodes {

    public static class Builder extends NodeBuilderAdapter {

        protected ClusterInstantiationNode clusterInstantiationNode;

        public Builder(NodeBuilder nodeBuilder) {
            super(nodeBuilder);
            nodeBuilder.attach(clusterInstantiationNode = new ClusterInstantiationNode());
        }

    }

    public static Builder cluster(NodeBuilder builder) {
        return new Builder(builder);
    }

}
