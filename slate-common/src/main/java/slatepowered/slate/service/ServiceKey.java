package slatepowered.slate.service;

/**
 * A service tag is a key/identifier used to dynamically
 * resolve/create (network) services.
 *
 * The queried service should be of type {@code T}.
 *
 * @param <T> The service instance type.
 */
public interface ServiceKey<T extends Service> {

    /**
     * Get the service class. This is the base
     * parameter of the service tag.
     *
     * @return The service class.
     */
    Class<T> getServiceClass();

    /**
     * Called when the service associated with this
     * tag is registered to the given manager.
     *
     * @param manager The manager.
     * @param service The service.
     */
    void register(ServiceManager manager, T service);

    /**
     * Used to get the tag for retrieving the local instance.
     *
     * @return The local tag, can be this.
     */
    default ServiceKey<T> toLocal() {
        return this;
    }

    static <T extends Service> ServiceKey<T> local(Class<T> tClass) {
        return new ServiceKey<T>() {
            @Override
            public Class<T> getServiceClass() {
                return tClass;
            }

            @Override
            public void register(ServiceManager manager, T service) {
                // noop
            }

            @Override
            public int hashCode() {
                return tClass.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (!(obj instanceof ServiceKey)) return false;
                return tClass == ((ServiceKey<?>)obj).getServiceClass();
            }
        };
    }

}
