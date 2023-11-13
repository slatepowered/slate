package slatepowered.slate.service;

import slatepowered.slate.service.singleton.SingletonKey;
import slatepowered.veru.misc.Throwables;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public interface ServiceProvider {

    Map<Class<?>, MethodHandle> TAG_METHOD_CACHE = new HashMap<>();

    /**
     * The parent service resolver to query the qualified
     * service key in.
     *
     * @return The service resolver.
     */
    ServiceProvider parentServiceResolver();

    /**
     * Checks and qualifies the given service key,
     * the returned service key is resolved in the network
     * service registry.
     *
     * @param <T> The return type of the key.
     * @param key The service key.
     * @throws UnsupportedOperationException If the given service key can not be qualified
     */
    <T extends Service> ServiceKey<T> qualifyServiceKey(ServiceKey<T> key) throws UnsupportedOperationException;

    /**
     * Get the service by the given key, this key will be
     * passed through the {@link #qualifyServiceKey(ServiceKey)}
     * method before being resolved by the network service manager.
     *
     * @param key The service key.
     * @return The service or null if absent.
     * @throws UnsupportedOperationException If the given service key can not be qualified
     */
    default <T extends Service> T getService(ServiceKey<T> key) throws UnsupportedOperationException {
        return this.parentServiceResolver().getService(qualifyServiceKey(key));
    }

    /**
     * Attempts to locally register the given service with
     * the given key.
     *
     * @param key The key.
     * @param service The service instance.
     * @param <T> The service type.
     */
    default <T extends Service> ServiceProvider register(ServiceKey<T> key, T service) {
        this.parentServiceResolver().register(qualifyServiceKey(key), service);
        return this;
    }

    @SuppressWarnings("unchecked")
    default <T extends Service> T getService(Class<T> tClass) {
        // try to get the service key from
        // the given class
        try {
            MethodHandle m = TAG_METHOD_CACHE.get(tClass);
            ServiceKey<?> key = null;
            if (m == null) {
                try {
                    // try for method
                    Method rm = tClass.getMethod("key");// probably cache this in the future
                    if (!ServiceKey.class.isAssignableFrom(rm.getReturnType()))
                        throw new NoSuchMethodException("Method `" + tClass.getName() + "." + rm.getName() + "` does not return ServiceKey");

                    m = MethodHandles.lookup().unreflect(rm);
                    TAG_METHOD_CACHE.put(tClass, m);
                } catch (NoSuchMethodException ignored) {

                } catch (Throwable t) {
                    Throwables.sneakyThrow(t);
                }

                try {
                    // try for method
                    Field rm = tClass.getField("KEY");// probably cache this in the future
                    if (!ServiceKey.class.isAssignableFrom(rm.getType()))
                        throw new NoSuchMethodException("Field `" + tClass.getName() + "." + rm.getName() + "` is not of type ServiceKey");

                    m = MethodHandles.lookup().unreflectGetter(rm);
                    TAG_METHOD_CACHE.put(tClass, m);
                } catch (NoSuchFieldException ignored) {

                } catch (Throwable t) {
                    Throwables.sneakyThrow(t);
                }
            }

            // no method/field found
            if (m == null) {
                throw new NoSuchMethodException();
            }

            key = (ServiceKey<?>) m.invoke();
            return (T) getService(key);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(tClass + " does not have a default `ServiceTag key()` method or `ServiceTag TAG` field");
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
            return null; // unreachable
        }
    }

    /** Get a singleton instance from the service manager. */
    default <T> T getSingleton(Class<T> valueClass) {
        return getService(SingletonKey.of(valueClass)).getValue();
    }

}
