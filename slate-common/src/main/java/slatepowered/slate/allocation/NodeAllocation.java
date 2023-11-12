package slatepowered.slate.allocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import slatepowered.slate.model.NodeComponent;

import java.util.List;

/**
 * Represents the data about an allocation of a node.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeAllocation implements NodeComponent, NodeAllocationResult {

    /**
     * The name of the node.
     */
    private String nodeName;

    /**
     * The components to register to the node.
     */
    private List<NodeComponent> components;

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public NodeAllocation successful() {
        return this;
    }

    @Override
    public FailedNodeAllocation failed() {
        return null;
    }

}
