package slatepowered.slate.service.network;

import lombok.RequiredArgsConstructor;
import slatepowered.slate.model.NamedRemote;
import slatepowered.slate.service.DynamicServiceKey;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceKey;
import slatepowered.slate.service.ServiceManager;

import java.util.function.BiFunction;

public interface NodeBoundServiceKey<T extends Service> extends ServiceKey<T> {

    /**
     * Set the name of the node.
     * 
     * @param name The name.
     * @return This.
     */
    NodeBoundServiceKey<T> forNode(String name);

    default NodeBoundServiceKey<T> forNode(NamedRemote remote) {
        forNode(remote.remoteChannelName());
        return this;
    }
    
}
