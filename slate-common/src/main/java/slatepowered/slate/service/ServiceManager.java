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
    private final Map<ServiceTag<?>, Service> localServices = new HashMap<>();

    public ServiceManager(ServiceManager parent) {
        this.parent = parent;
    }

    public ServiceManager() {
        this(null);
    }

    /**
     * Retrieves the given service from this service manager.
     *
     * @param tag The service tag.
     * @param <T> The service instance type.
     * @return The service instance.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Service> T getService(ServiceTag<T> tag) throws UnsupportedOperationException {
        Service service;

        // check local service registry
        service = localServices.get(tag);
        if (service != null) {
            return (T) service;
        }

        // create dynamically
        if (tag instanceof DynamicServiceTag) {
            DynamicServiceTag<T> serviceTag = (DynamicServiceTag<T>) tag;
            return serviceTag.create(this);
        }

        // find in parent
        if (parent != null) {
            return parent.getService(tag);
        }

        return null;
    }

    /**
     * Registers the given service locally to this service manager.
     *
     * @param tag The tag to register it under.
     * @param service The service instance.
     * @param <T> The service instance type.
     * @return This.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Service> ServiceManager register(ServiceTag<T> tag, T service) {
        localServices.put(tag, service);
        tag.register(this, service);
        return this;
    }

    @Override
    public <T extends Service> ServiceTag<T> qualifyServiceTag(ServiceTag<T> tag) throws UnsupportedOperationException {
        return tag;
    }

    @Override
    public ServiceResolver parentServiceResolver() {
        return parent;
    }

}
