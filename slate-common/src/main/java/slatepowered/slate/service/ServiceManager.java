package slatepowered.slate.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Locally manages local, remote and dynamic services for
 * the current runtime environment.
 */
public class ServiceManager implements ServiceResolver {

    /** The parent service manager. */
    private final ServiceManager parent;

    /** All registered local services. */
    private final Map<ServiceKey<?>, Service> localServices = new HashMap<>();

    public ServiceManager(ServiceManager parent) {
        this.parent = parent;
    }

    public ServiceManager() {
        this(null);
    }

    /**
     * Retrieves the given service from this service manager.
     *
     * @param key The service key.
     * @param <T> The service instance type.
     * @return The service instance.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Service> T getService(ServiceKey<T> key) throws UnsupportedOperationException {
        Service service;

        // check local service registry
        service = localServices.get(key.toLocal());
        if (service != null) {
            return (T) service;
        }

        // create dynamically
        if (key instanceof DynamicServiceKey) {
            DynamicServiceKey<T> serviceTag = (DynamicServiceKey<T>) key;
            return serviceTag.create(this);
        }

        // find in parent
        if (parent != null) {
            return parent.getService(key);
        }

        return null;
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

    @Override
    public <T extends Service> ServiceKey<T> qualifyServiceTag(ServiceKey<T> key) throws UnsupportedOperationException {
        return key;
    }

    @Override
    public ServiceResolver parentServiceResolver() {
        return parent;
    }

}
