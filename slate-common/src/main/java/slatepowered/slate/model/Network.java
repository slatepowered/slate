package slatepowered.slate.model;

import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceManager;
import slatepowered.slate.service.ServiceResolver;
import slatepowered.slate.service.ServiceTag;

/**
 * Represents a Slate network.
 */
public interface Network extends ServiceResolver {

    /**
     * Get the network's local service manager.
     *
     * @return The service manager.
     */
    ServiceManager serviceManager();

    /**
     * Get the master node information.
     *
     * @return The master.
     */
    Node master();

    @Override
    default <T extends Service> ServiceTag<T> qualifyServiceTag(ServiceTag<T> tag) throws UnsupportedOperationException {
        return tag;
    }

    @Override
    default ServiceResolver parentServiceResolver() {
        return serviceManager();
    }

}
