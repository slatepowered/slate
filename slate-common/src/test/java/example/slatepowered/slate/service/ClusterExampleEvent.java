package example.service.api;

import lombok.Data;
import slatepowered.reco.rpc.event.ObjectEventPayload;

@Data
public class ClusterExampleEvent implements ObjectEventPayload {

    private final String clusterName;
    private final int action;

    @Override
    public Object getRemoteObjectUID() {
        return clusterName;
    }
    
}
