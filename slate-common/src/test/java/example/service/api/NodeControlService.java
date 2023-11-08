package example.service.api;

import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.slate.model.NamedRemote;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceTag;
import slatepowered.slate.service.remote.RemoteServiceTag;

import java.util.concurrent.CompletableFuture;

public interface NodeControlService extends RemoteAPI, Service {

    ServiceTag<NodeControlService> TAG = ServiceTag.local(NodeControlService.class);

    static ServiceTag<NodeControlService> remote(String node) {
        return RemoteServiceTag.remote(NodeControlService.class)
                .forRemote(node);
    }

    static ServiceTag<NodeControlService> remote(NamedRemote node) {
        return RemoteServiceTag.remote(NodeControlService.class)
                .forRemote(node);
    }

    void start();

    CompletableFuture<Void> startAsync();

}
