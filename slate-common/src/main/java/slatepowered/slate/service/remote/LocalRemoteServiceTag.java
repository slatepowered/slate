package slatepowered.slate.service.remote;

import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceManager;
import slatepowered.slate.service.ServiceTag;

public interface LocalRemoteServiceTag<T extends Service & RemoteAPI> extends ServiceTag<T> {

    @Override
    default void register(ServiceManager manager, T service) {
        // register the service instance as a handler locally
        manager.getService(RPCService.TAG).register(service);
    }

}
