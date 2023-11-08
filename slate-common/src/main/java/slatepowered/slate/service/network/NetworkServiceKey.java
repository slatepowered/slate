package slatepowered.slate.service.network;

import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceKey;
import slatepowered.slate.service.remote.RemoteServiceKey;

/**
 * A service key which represents network services.
 *
 * These are services provided to the whole network by one
 * master node, usually the `master` node (so the master),
 * but can also be implemented locally with the same key.
 *
 * @param <T> The service type.
 */
public class NetworkServiceKey<T extends Service & RemoteAPI> extends RemoteServiceKey<T> {

    /**
     * The service class.
     */
    private final Class<T> serviceClass;

    /**
     * The node which always provides it.
     */
    private final String provider;

    /**
     * The local key instance.
     */
    private final ServiceKey<T> localKey;

    NetworkServiceKey(Class<T> serviceClass, String provider) {
        this.serviceClass = serviceClass;
        this.provider = provider;
        this.localKey = ServiceKey.local(serviceClass);
        forRemote(provider);
    }

    public static <T extends Service & RemoteAPI> NetworkServiceKey<T> provided(Class<T> tClass,
                                                                                String provider) {
        return new NetworkServiceKey<>(tClass, provider);
    }

    @Override
    public Class<T> getServiceClass() {
        return serviceClass;
    }

    public String getProviderRemote() {
        return provider;
    }

    @Override
    public ServiceKey<T> toLocal() {
        return localKey;
    }

}
