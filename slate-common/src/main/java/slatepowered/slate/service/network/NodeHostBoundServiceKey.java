package slatepowered.slate.service.network;

import lombok.RequiredArgsConstructor;
import slatepowered.slate.model.Network;
import slatepowered.slate.model.Node;
import slatepowered.slate.service.*;

import java.util.function.BiFunction;

public interface NodeHostBoundServiceKey<T extends Service> extends NodeBoundServiceKey<T> {

    /**
     * Set the host this should be bound to.
     *
     * @param hostName The host name.
     * @return The key.
     */
    NodeHostBoundServiceKey<T> forHost(String hostName);

    /**
     * A node-host-bound service mapping from S -> R.
     */
    @RequiredArgsConstructor
    @SuppressWarnings("unchecked")
    class Mapped<S extends Service, R extends Service> implements NodeHostBoundServiceKey<R>, DynamicServiceKey<R> {
        private final Class<R> serviceClass;
        private final ServiceKey<S> sourceKey;
        private final BiFunction<S, String, R> function;
        private String hostName;
        private String nodeName;

        @Override
        public Class<R> getServiceClass() {
            return serviceClass;
        }

        @Override
        public void register(ServiceManager manager, R service) {
            throw new UnsupportedOperationException("Can not register key: " + this);
        }

        @Override
        public ServiceKey<R> toLocal() {
            return (ServiceKey<R>) sourceKey.toLocal();
        }

        @Override
        public NodeBoundServiceKey<R> forNode(String name) {
            this.nodeName = name;
            return this;
        }

        @Override
        public R create(ServiceProvider manager) {
            Node hostNode = manager.getService(Network.KEY).getNode(hostName);
            S baseService = manager.getService(hostNode.qualifyServiceKey(sourceKey));
            return function.apply(baseService, nodeName);
        }

        @Override
        public NodeHostBoundServiceKey<R> forHost(String hostName) {
            this.hostName = hostName;
            return this;
        }
    }

    /**
     * Creates a mapped, node-bound service key for the given
     * result service class.
     *
     * @param serviceClass The result service class.
     * @param key The source key.
     * @param function The mapping function.
     * @param <S> The source service type.
     * @param <R> The result service type.
     * @return The mapped service key.
     */
    static <S extends Service, R extends Service> Mapped<S, R> mapped(Class<R> serviceClass, ServiceKey<S> key, BiFunction<S, String, R> function) {
        return new Mapped<>(serviceClass, key, function);
    }

}
