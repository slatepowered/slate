package slatepowered.slate.service;

/**
 * Dynamically creates the service for the given service
 * manager when queried.
 *
 * @see ServiceKey
 * @param <T>
 */
public interface DynamicServiceKey<T extends Service> extends ServiceKey<T> {

    /**
     * Dynamically create the service for the given service manager.
     *
     * @param manager The manager.
     * @return The service.
     */
    T create(ServiceManager manager);

}
