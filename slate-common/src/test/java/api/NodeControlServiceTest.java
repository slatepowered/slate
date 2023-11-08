package api;

import slatepowered.slate.service.ServiceManager;

import java.util.concurrent.CompletableFuture;

public class NodeControlServiceTest {

    void test_Master() {
        ServiceManager manager = new ServiceManager();

        manager.register(NodeControlService.TAG, new NodeControlService() {
            @Override
            public void start() {
                System.out.println("starting node!");
            }

            @Override
            public CompletableFuture<Void> startAsync() {
                return CompletableFuture.runAsync(this::start);
            }
        });

        manager.getService(NodeControlService.TAG).start();
        manager.getService(NodeControlService.class).start();

        manager.getService(NodeControlService.remote("kitpvp"))
                .startAsync()
                .whenComplete((_a, _b) -> {
                    System.out.println("started kitpvp");
                });
    }

    void test_NodeA() {
        ServiceManager manager = new ServiceManager();

        manager.getService(NodeControlService.remote("survival"))
                .startAsync();
    }

}
