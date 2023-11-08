package slatepowered.slate.service;

/**
 * Dynamically creates the service for the given service
 * manager when queried.
 *
 * @see ServiceTag
 * @param <T>
 */
public interface DynamicServiceTag<T extends Service> extends ServiceTag<T> {

    /**
     * Dynamically create the service for the given service manager.
     *
     * @param manager The manager.
     * @return The service.
     */
    T create(ServiceManager manager);

}
