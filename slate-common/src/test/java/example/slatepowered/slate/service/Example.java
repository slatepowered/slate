package example.service.api;

import slatepowered.reco.rpc.event.RemoteEvent;
import slatepowered.slate.service.ServiceManager;

import java.util.concurrent.CompletableFuture;

public class Example {

    /**
     * This code would be run on the master during
     * setup which registers the service locally
     * and as an RPC API.
     */
    public void runOnMaster() {
        /* Some service manager from somewhere */
        ServiceManager serviceManager = new ServiceManager();

        serviceManager.register(ExampleService.key(), new ExampleService() {
            /*
                The implementation of ExampleService,
                all these implementations will be called remotely as well
                if permitted.

                Imagine an actual, working implementation here.
             */

            @Override public void log(String msg) { System.out.println("logged remotely: " + msg); }
            @Override public CompletableFuture<Void> logAsync(String msg) { return null; }
            @Override public RemoteEvent<ClusterExampleEvent> onClusterEvent() { return null; }
            @Override public RemoteCluster getCluster(String name) { return null; }
            @Override public int getNodeCount(String clusterName) { return 0; }
        });
    }

    /**
     * This code would be run on some other node
     * in the network, with permissions to communicate
     * with the master and invoke all methods used here remotely.
     */
    public void runOnOtherNode() {
        /* Some service manager from somewhere */
        ServiceManager serviceManager = new ServiceManager();

        ExampleService exampleService = serviceManager.getService(ExampleService.key());
        exampleService.logAsync("Hello World!");
        exampleService.onClusterEvent().then(e -> System.out.println("cluster event: " + e.getClusterName()));
        RemoteCluster cluster = exampleService.getCluster("veryImportantCluster");
        cluster.onClusterEvent().then(e -> System.out.println("veryImportantCluster just had something happen uh oh"));
        System.out.println("current nodes allocated on veryImportantCluster: " + cluster.getNodeCount());
    }

}
