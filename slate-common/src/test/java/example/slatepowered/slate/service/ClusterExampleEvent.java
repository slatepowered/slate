package example.slatepowered.slate.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import slatepowered.reco.rpc.event.ObjectEvent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterExampleEvent implements ObjectEvent {

    private String clusterName;
    private int action;

    @Override
    public Object getRemoteObjectUID() {
        return clusterName;
    }

}
