package slatepowered.slate.service;

import slatepowered.veru.misc.Throwables;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public interface ServiceResolver {

    Map<Class<?>, MethodHandle> TAG_METHOD_CACHE = new HashMap<>();

    /**
     * The parent service resolver to query the qualified
     * service tag in.
     *
     * @return The service resolver.
     */
    ServiceResolver parentServiceResolver();

    /**
     * Checks and qualifies the given service tag,
     * the returned service tag is resolved in the network
     * service registry.
     *
     * @param <T> The return type of the tag.
     * @param tag The service tag.
     * @throws UnsupportedOperationException If the given service tag can not be qualified
     */
    <T extends Service> ServiceTag<T> qualifyServiceTag(ServiceTag<T> tag) throws UnsupportedOperationException;

    /**
     * Get the service by the given tag, this tag will be
     * passed through the {@link #qualifyServiceTag(ServiceTag)}
     * method before being resolved by the network service manager.
     *
     * @param tag The service tag.
     * @return The service or null if absent.
     * @throws UnsupportedOperationException If the given service tag can not be qualified
     */
    default <T extends Service> T getService(ServiceTag<T> tag) throws UnsupportedOperationException {
        return this.parentServiceResolver().getService(qualifyServiceTag(tag));
    }

    @SuppressWarnings("unchecked")
    default <T extends Service> T getService(Class<T> tClass) {
        // try to get the service tag from
        // the given class
        try {
            MethodHandle m = TAG_METHOD_CACHE.get(tClass);
            if (m == null) {
                try {
                    // try for method
                    Method rm = tClass.getMethod("getTag");// probably cache this in the future
                    if (!ServiceTag.class.isAssignableFrom(rm.getReturnType()))
                        throw new NoSuchMethodException("Method `" + tClass.getName() + "." + rm.getName() + "` does not return ServiceTag");

                    m = MethodHandles.lookup().unreflect(rm);
                    TAG_METHOD_CACHE.put(tClass, m);
                } catch (NoSuchMethodException ignored) {

                } catch (Throwable t) {
                    Throwables.sneakyThrow(t);
                }

                try {
                    // try for method
                    Field rm = tClass.getField("TAG");// probably cache this in the future
                    if (!ServiceTag.class.isAssignableFrom(rm.getType()))
                        throw new NoSuchMethodException("Field `" + tClass.getName() + "." + rm.getName() + "` is not of type ServiceTag");

                    m = MethodHandles.lookup().unreflectGetter(rm);
                    TAG_METHOD_CACHE.put(tClass, m);
                } catch (NoSuchMethodException ignored) {

                } catch (Throwable t) {
                    Throwables.sneakyThrow(t);
                }

                // no method/field found
                if (m == null)
                    throw new NoSuchMethodException();
            }

            return getService((ServiceTag<T>) m.invoke());
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(tClass + " does not have a default `ServiceTag tag()` method or `ServiceTag TAG` field");
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
            return null; // unreachable
        }
    }

}
