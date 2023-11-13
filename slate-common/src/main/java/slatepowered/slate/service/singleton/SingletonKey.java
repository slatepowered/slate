package slatepowered.slate.service.singleton;

import lombok.AllArgsConstructor;
import slatepowered.slate.service.ServiceKey;
import slatepowered.slate.service.ServiceManager;

import java.util.Objects;

/**
 * A singleton instance key which you can query in a service manager
 * to get a singleton container.
 *
 * @param <T> The service type.
 */
@SuppressWarnings("rawtypes")
@AllArgsConstructor
public class SingletonKey<T> implements ServiceKey<SingletonContainer<T>> {

    public static <T> SingletonKey<T> of(Class<T> valueClass) {
        return new SingletonKey<>(valueClass);
    }

    /**
     * The value class.
     */
    private Class<?> valueClass;

    @Override
    @SuppressWarnings("unchecked")
    public Class<SingletonContainer<T>> getServiceClass() {
        return (Class<SingletonContainer<T>>)(Object) SingletonContainer.class;
    }

    @Override
    public void register(ServiceManager manager, SingletonContainer service) {

    }

    public Class<?> getValueClass() {
        return valueClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingletonKey<?> that = (SingletonKey<?>) o;
        return Objects.equals(valueClass, that.valueClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueClass);
    }

}
