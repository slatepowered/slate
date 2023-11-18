package slatepowered.slate.service;

import slatepowered.slate.model.Network;
import slatepowered.slate.service.singleton.SingletonContainer;
import slatepowered.slate.service.singleton.SingletonKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Locally manages local, remote and dynamic services for
 * the current runtime environment.
 */
public class ServiceManager implements ServiceProvider {

    /** The network which provides this service manager. */
    private final Network network;

    /** The parent service manager. */
    private final ServiceManager parent;

    /** All registered local services. */
    private final Map<ServiceKey<?>, Service> localServices = new HashMap<>();

    public ServiceManager(Network network, ServiceManager parent) {
        this.network = network;
        this.parent = parent;
    }

    public ServiceManager(ServiceManager parent) {
        this(parent.getNetwork(), parent);
    }

    public ServiceManager(Network network) {
        this(network, null);
    }

    /**
     * Get the local network instance this service manager is attached to.
     *
     * @return The network instance.
     */
    public Network getNetwork() {
        return network;
    }

    @Override
    public ServiceManager serviceManager() {
        return this;
    }

    /**
     * Retrieves the given service from this service manager with the
     * given provider as source.
     *
     * @param key The service key.
     * @param provider The service provider used to invoke this.
     * @param <T> The service instance type.
     * @return The service instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends Service> T getService(ServiceKey<T> key, ServiceProvider provider) throws UnsupportedOperationException {
        Service service;
        System.out.println("Service: Finding service for key(" + key + ") in manager(" + this + ")");

        // check local service registry
        service = localServices.get(key.toLocal());
        if (service != null) {
            System.out.println("Service: found local service for key(" + key + "): " + service);
            return (T) service;
        }

        // create dynamically
        if (key instanceof DynamicServiceKey) {
            System.out.println("Service: creating service from dynamic service key: " + key);
            DynamicServiceKey<T> serviceTag = (DynamicServiceKey<T>) key;
            return serviceTag.create(provider == null ? this : provider);
        }

        // find in parent
        if (parent != null) {
            System.out.println("Service: attempting to find service in parent(" + parent + ")");
            return parent.getService(key, provider);
        }

        System.out.println("Service: could not find service, returning null");
        return null;
    }

    /**
     * Retrieves the given service from this service manager with the
     * given provider as source.
     *
     * @param key The service key.
     * @param <T> The service instance type.
     * @return The service instance.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Service> T getService(ServiceKey<T> key) throws UnsupportedOperationException {
        return getService(key, null);
    }

    /**
     * Registers the given service locally to this service manager.
     *
     * @param key The key to register it under.
     * @param service The service instance.
     * @param <T> The service instance type.
     * @return This.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Service> ServiceManager register(ServiceKey<T> key, T service) {
        ServiceKey<T> localKey = key.toLocal();
        localServices.put(localKey, service);
        localKey.register(this, service);
        return this;
    }

    public <T> ServiceManager registerSingleton(Class<T> tClass, T value) {
        return register(SingletonKey.of(tClass), new SingletonContainer<T>().value(value));
    }

    @Override
    public <T extends Service> ServiceKey<T> qualifyServiceKey(ServiceKey<T> key) throws UnsupportedOperationException {
        return key;
    }

    @Override
    public ServiceProvider parentServiceResolver() {
        return parent;
    }

}
