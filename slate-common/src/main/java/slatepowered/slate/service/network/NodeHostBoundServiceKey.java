package slatepowered.slate.service.network;

import lombok.RequiredArgsConstructor;
import slatepowered.slate.service.DynamicServiceKey;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceKey;
import slatepowered.slate.service.ServiceManager;

import java.util.function.BiFunction;

public interface NodeHostBoundServiceKey<T extends Service> extends NodeBoundServiceKey<T> {

    /**
     * A node-bound service mapping from S -> R.
     */
    @RequiredArgsConstructor
    @SuppressWarnings("unchecked")
    class Mapped<S extends Service, R extends Service> implements NodeHostBoundServiceKey<R>, DynamicServiceKey<R> {
        private final Class<R> serviceClass;
        private final ServiceKey<S> sourceKey;
        private final BiFunction<String, S, R> function;
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
        public R create(ServiceManager manager) {
            return function.apply(nodeName, manager.getService(sourceKey));
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
    static <S extends Service, R extends Service> Mapped<S, R> mapped(Class<R> serviceClass, ServiceKey<S> key, BiFunction<String, S, R> function) {
        return new Mapped<>(serviceClass, key, function);
    }

}
