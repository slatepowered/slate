package example.slatepowered.slate.service;

import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.reco.rpc.event.RemoteEvent;
import slatepowered.reco.rpc.function.Allow;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.network.NetworkServiceKey;

import java.util.concurrent.CompletableFuture;

/**
 * We will make this service a network service by making
 * it a remote API and using a {@link NetworkServiceKey}.
 */
public interface ExampleService extends Service, RemoteAPI {

    static NetworkServiceKey<ExampleService> key() {
        // the service is always provided by the master node,
        // so the `provider` parameter can always be set to `master`
        return NetworkServiceKey.provided(ExampleService.class, "master");
    }

    /**
     * Will log the given message to the master console.
     *
     * The @Allow annotation determines what security groups
     * are allowed to call this method remotely: basically, the
     * RPC system has a security system to prevent any insignificant
     * node from calling very important functions.
     *
     * A security group is a tag which can be added to any RPC sender
     * by the receiver, which then allows that sender to remotely invoke/access
     * any resources with the aforementioned security group set to allow.
     *
     * `all` is a special security group, which covers all RPC senders.
     * This should really only be done for simple data APIs, like getters
     * which retrieve trivial information.
     *
     * The @Allow annotation can only be used in the base API declaration
     * and not in any remote objects provided by that API as they all call
     * back to these methods regardless.
     */
    @Allow("all")
    void log(String msg);

    CompletableFuture<Void> logAsync(String msg);

    /**
     * Get an event handler which is called every time
     * any cluster posts the event.
     *
     * @return The event you can subscribe to.
     */
    RemoteEvent<ClusterExampleEvent> onClusterEvent();

    /**
     * When called, the singular `name` parameter will be accepted
     * as the object's UID and a new proxy object is created to
     * represent the remote object. Each method call on the remote object
     * is forwarded here, to a method with the same name unless otherwise
     * specified.
     */
    RemoteCluster getCluster(String name);

    @Allow("all")
    int getNodeCount(String clusterName);

}
