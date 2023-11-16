package slatepowered.slate.communication;

import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.rmq.RMQProvider;
import slatepowered.reco.serializer.KryoSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A communication strategy implementation which utilizes one RabbitMQ node.
 */
public class RMQCommunicationStrategy extends CommunicationStrategy {

    /**
     * The RabbitMQ connection.
     */
    private final Channel rmqChannel;

    public RMQCommunicationStrategy(Channel rmqChannel) {
        this.rmqChannel = rmqChannel;
    }

    @Override
    protected CommunicationProvider<?> createCommunicationProvider(CommunicationKey key) throws Exception {
        // compile communication key
        if (!(key instanceof RMQCommunicationKey)) {
            if (key == CommunicationKey.clusterDeclare()) {
                key = new RMQCommunicationKey("slate.clusterDeclare");
            }

            throw new IllegalArgumentException("Unsupported communication key of type(" + key.getClass() + "): " + key);
        }

        return new RMQProvider(localName, KryoSerializer.standard())
                .connect(rmqChannel)
                .bind(((RMQCommunicationKey)key).getExchangeName());
    }

    @Override
    public CommunicationKey createKey() {
        return new RMQCommunicationKey("slatenetwork:" +
                Integer.toHexString((short)(Math.random() * Short.MAX_VALUE)));
    }

    @Override
    public RMQCommunicationStrategy localName(String localName) {
        super.localName(localName);
        return this;
    }

    /**
     * The strategy builder.
     */
    @RequiredArgsConstructor
    public static class Builder {
        private final String localName;
        private Channel rmqChannel;
        private String host;
        private int port = 5672;
        private String username;
        private String password;
        private String virtualHost = "/";

        public RMQCommunicationStrategy build() {
            try {
                return new RMQCommunicationStrategy(rmqChannel == null ?
                        RMQProvider.makeConnection(host, port, username, password, virtualHost) :
                        rmqChannel)
                        .localName(localName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create RabbitMQ communication strategy", e);
            }
        }

        public Builder rmqChannel(Channel rmqChannel) {
            this.rmqChannel = rmqChannel;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder virtualHost(String virtualHost) {
            this.virtualHost = virtualHost;
            return this;
        }
    }

    public static Builder builder(String localName) {
        return new Builder(localName);
    }

}
