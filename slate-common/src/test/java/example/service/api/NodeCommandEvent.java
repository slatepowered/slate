package example.service.api;

import lombok.Data;
import slatepowered.reco.rpc.event.ObjectEventPayload;

@Data
public class NodeCommandEvent implements ObjectEventPayload {

    private final String node;
    private final String command;

    @Override
    public Object getRemoteObjectUID() {
        return node;
    }

}
