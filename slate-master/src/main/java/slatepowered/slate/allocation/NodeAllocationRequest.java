package slatepowered.slate.allocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import slatepowered.slate.model.SharedNodeComponent;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NodeAllocationRequest {

    /**
     * The name of the parent node.
     */
    private String parentNodeName;

    /**
     * The name of the node.
     */
    private String nodeName;

    /**
     * The tags on the node.
     */
    private String[] tags;

    /**
     * The node components to be shared.
     */
    private List<SharedNodeComponent> components;

}
