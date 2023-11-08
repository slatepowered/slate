package example.service.api;

import slatepowered.reco.rpc.event.RemoteEvent;
import slatepowered.reco.rpc.objects.ObjectMethod;
import slatepowered.reco.rpc.objects.RemoteObject;
import slatepowered.reco.rpc.objects.UID;

import java.util.concurrent.CompletableFuture;

public interface NodeControl extends RemoteObject<NodeControlService> {

    @UID
    String getName();

    @ObjectMethod
    void start();

    @ObjectMethod
    CompletableFuture<Void> startAsync();

    @ObjectMethod
    RemoteEvent<NodeCommandEvent> onCommand();

}
