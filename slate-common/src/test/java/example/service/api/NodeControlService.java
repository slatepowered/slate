package example.service.api;

import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.reco.rpc.event.RemoteEvent;
import slatepowered.slate.model.NamedRemote;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceTag;
import slatepowered.slate.service.remote.RemoteServiceTag;

import java.util.concurrent.CompletableFuture;

public interface NodeControlService extends RemoteAPI, Service {

    ServiceTag<NodeControlService> TAG = ServiceTag.local(NodeControlService.class);

    static ServiceTag<NodeControlService> remote() {
        return RemoteServiceTag.remote(NodeControlService.class)
                .forRemote("master");
    }

    void start(String node);

    CompletableFuture<Void> startAsync(String node);

    RemoteEvent<NodeCommandEvent> onCommand();

    NodeControl forNode(String node);

}
