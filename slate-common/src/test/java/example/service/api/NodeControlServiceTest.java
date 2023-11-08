package example.service.api;

import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.event.RemoteEvent;
import slatepowered.slate.service.ServiceManager;

import java.util.concurrent.CompletableFuture;

public class NodeControlServiceTest {

    void test_Master() {
        ServiceManager manager = new ServiceManager();

        manager.register(NodeControlService.TAG, new NodeControlService() {
            @Inject
            RPCManager rpcManager;

            @Override
            public void start(String name) {
                System.out.println("starting node: " + name);
            }

            @Override
            public CompletableFuture<Void> startAsync(String name) {
                return CompletableFuture.runAsync(() -> start(name));
            }

            @Override
            public RemoteEvent<NodeCommandEvent> onCommand() {
                // you could replace this with a local instance
                // todo: make it automatically call this local instance too
                //  when you call invokeRemoteEvent, or maybe only do that
                //  in a method RPCManager#invokeEvent
                return null;
            }

            @Override
            public NodeControl forNode(String node) {
                // you could return a local representation if
                // you wanted to but im too lazy to do that rn
                return null;
            }

            public void sendCommand(String str) {
                rpcManager.invokeRemoteEvent(NodeControlService.class, "onCommand", str);
            }
        });
    }

    void test_NodeA() {
        ServiceManager manager = new ServiceManager();

        NodeControlService service = manager.getService(NodeControlService.remote());
        NodeControl survivalControl = service.forNode("survival");
        survivalControl.start();
        survivalControl.onCommand().then(s -> System.out.println("command ran on survival: " + s.getCommand()));
    }

}
