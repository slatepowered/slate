package slatepowered.slate.service.network;

import slatepowered.slate.model.NamedRemote;

public interface NodeBoundServiceKey {

    /**
     * Set the name of the node.
     * 
     * @param name The name.
     * @return This.
     */
    NodeBoundServiceKey forNode(String name);

    default NodeBoundServiceKey forNode(NamedRemote remote) {
        forNode(remote.remoteChannelName());
        return this;
    }
    
}
