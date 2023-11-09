package example.slatepowered.slate.service;

import slatepowered.reco.rpc.event.RemoteEvent;
import slatepowered.reco.rpc.objects.ObjectMethod;
import slatepowered.reco.rpc.objects.RemoteObject;
import slatepowered.reco.rpc.objects.UID;

/**
 * This is a remote object, each remote object has a UID
 * which is the only data actually stored locally and is
 * also the only way to identify objects across the network.
 */
public interface RemoteCluster extends RemoteObject<ExampleService> {

    /**
     * The @UID annotation denotes this method as providing
     * the cluster's UID.
     */
    @UID
    String getName();

    /**
     * This method will call {@link ExampleService#getNodeCount(String)}
     * with the object's UID (in this case the cluster's name), which
     * then invokes that method on the remote client finally returning
     * the result.
     */
    @ObjectMethod
    int getNodeCount();

    /**
     * This method will return a remote event you can subscribe
     * to, which only handles events for this specific object (determined
     * by the UID).
     *
     * This only works if the event implements {@link slatepowered.reco.rpc.event.ObjectEvent}
     * because the {@link slatepowered.reco.rpc.event.ObjectEvent#getRemoteObjectUID()} method is
     * used to determine the UID of the object the event is about which is then
     * matched to this object when the event is received locally.
     */
    @ObjectMethod
    RemoteEvent<ClusterExampleEvent> onClusterEvent();

}
