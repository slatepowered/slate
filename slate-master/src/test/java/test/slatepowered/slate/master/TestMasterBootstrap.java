package test.slatepowered.slate.master;

import slatepowered.reco.rmq.RMQProvider;
import slatepowered.slate.communication.RMQCommunicationKey;
import slatepowered.slate.communication.RMQCommunicationStrategy;
import slatepowered.slate.master.Master;

public class TestMasterBootstrap {

    public static void main(String[] args) throws Throwable {
        RMQCommunicationStrategy communicationStrategy = new RMQCommunicationStrategy(
                RMQProvider.makeConnection("127.0.0.1", 5672, "guest", "guest", "/"));

        Master master = Master.builder()
                .communicationKey(RMQCommunicationKey.named("test"))
                .communicationStrategy(communicationStrategy.localName("master"))
                .build();
    }

}
