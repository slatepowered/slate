package slatepowered.slate.service.remote;

import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceManager;
import slatepowered.slate.service.ServiceKey;

public interface LocalRemoteServiceKey<T extends Service & RemoteAPI> extends ServiceKey<T> {

    static <T extends Service & RemoteAPI> LocalRemoteServiceKey<T> key(Class<T> tClass) {
        return new LocalRemoteServiceKey<T>() {
            @Override
            public Class<T> getServiceClass() {
                return tClass;
            }
        };
    }

    @Override
    default void register(ServiceManager manager, T service) {
        // register the service instance as a handler locally
        manager.getSingleton(RPCManager.class).register(service);
    }

}
