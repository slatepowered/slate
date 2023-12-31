package example.slatepowered.slate.service;

import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.event.RemoteEvent;
import slatepowered.slate.model.Network;
import slatepowered.slate.model.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class Example {

    /**
     * This code would be run on the master during
     * setup which registers the service locally
     * and as an RPC API.
     */
    public void runOnMaster(Network net) {
        net.register(ExampleService.key(), new ExampleService() {
            /*
                The implementation of ExampleService,
                all these implementations will be called remotely as well
                if permitted.

                Imagine an actual, working implementation here.
             */

            Map<String, Integer> clusterNodeCounts = new HashMap<>();

            @Override public void log(String msg) { System.out.println("Logged Remotely: " + msg); }
            @Override public CompletableFuture<Void> logAsync(String msg) { return null; }
            @Override public RemoteEvent<ClusterExampleEvent> onClusterEvent() { return null; }
            @Override public RemoteCluster getCluster(String name) { return null; }
            @Override public int getNodeCount(String clusterName) { return clusterNodeCounts.compute(clusterName, (__, old) -> old == null ? new Random().nextInt(10) : old + 1); }
        });
    }

    /**
     * This code would be run on some other node
     * in the network, with permissions to communicate
     * with the master and invoke all methods used here remotely.
     */
    public void runOnOtherNode(Network net) {
        ExampleService exampleService = net.getService(ExampleService.key());
        exampleService.logAsync("Hello World!");
        exampleService.onClusterEvent().then(e -> System.out.println("cluster event: " + e.getClusterName()));
        RemoteCluster cluster = exampleService.getCluster("veryImportantCluster");
        cluster.onClusterEvent().then(e -> System.out.println("veryImportantCluster just had something happen uh oh"));
        System.out.println("current nodes allocated on veryImportantCluster: " + cluster.getNodeCount());
    }

    public static void main(String[] args) {
        final String RMQ_HOST = "127.0.0.1";
        final int RMQ_PORT = 5672;
        final String RMQ_USER = "guest";
        final String RMQ_PASSWORD = "guest";
        final String RMQ_VHOST = "/";

        // create network and communication provider for the master node
        Network networkOnMaster = null; // todo
        networkOnMaster.registerNode(new Node("other", networkOnMaster) {
            @Override
            public String[] getTags() {
                return new String[] { "node", "*" };
            }
        });

        // create network and communication provider for the other node
        Network networkOnOther = null;

        // invoke methods
        Example instance = new Example();
        instance.runOnMaster(networkOnMaster);
        instance.runOnOtherNode(networkOnOther);

        // call cluster event
        networkOnMaster.getSingleton(RPCManager.class).invokeRemoteEvent(ExampleService.class, "onClusterEvent", new ClusterExampleEvent("aCluster", 0));
        networkOnMaster.getSingleton(RPCManager.class).invokeRemoteEvent(ExampleService.class, "onClusterEvent", new ClusterExampleEvent("veryImportantCluster", 1));
    }

}
