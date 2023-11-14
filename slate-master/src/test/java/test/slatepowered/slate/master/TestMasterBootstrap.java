package test.slatepowered.slate.master;

import slatepowered.reco.rmq.RMQProvider;
import slatepowered.slate.allocation.NodeAllocator;
import slatepowered.slate.communication.RMQCommunicationKey;
import slatepowered.slate.communication.RMQCommunicationStrategy;
import slatepowered.slate.master.Master;
import slatepowered.slate.model.NodeBuilder;
import slatepowered.slate.packages.key.JavaPackageKey;
import slatepowered.veru.io.FileUtil;
import slatepowered.veru.runtime.JavaVersion;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestMasterBootstrap {

    public static void main(String[] args) throws Throwable {
        final Path dir = Paths.get("./test");
        FileUtil.deleteIfPresent(dir);
        FileUtil.createDirectoryIfAbsent(dir);

        RMQCommunicationStrategy communicationStrategy = new RMQCommunicationStrategy(
                RMQProvider.makeConnection("127.0.0.1", 5672, "guest", "guest", "/"));

        Master master = Master.builder()
                .communicationKey(RMQCommunicationKey.named("test"))
                .communicationStrategy(communicationStrategy.localName("master"))
                .directory(dir)
                .build();

        {
            NodeBuilder builder = master.master().child("test");
            builder.attach(master.getIntegratedCluster().getService(NodeAllocator.class));
            builder.attach(JavaPackageKey.jdk(JavaVersion.JAVA_8).attachment());
            builder.build().initialize().whenComplete((initializationResult, throwable) -> {
                System.out.println("initialized node");
            });
        }
    }

}
