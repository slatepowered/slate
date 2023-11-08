package slatepowered.slate.service.remote;

import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.slate.model.NamedRemote;
import slatepowered.slate.service.DynamicServiceTag;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceManager;

/**
 * todo
 */
public abstract class RemoteServiceTag<T extends Service & RemoteAPI> implements DynamicServiceTag<T> {

    public static <T extends Service & RemoteAPI> RemoteServiceTag<T> remote(Class<T> tClass) {
        return new RemoteServiceTag<T>() {
            @Override
            public Class<T> getServiceClass() {
                return tClass;
            }
        };
    }

    /** The remote channel name */
    protected String remoteName;

    public RemoteServiceTag<T> forRemote(String remoteName) {
        this.remoteName = remoteName;
        return this;
    }

    public RemoteServiceTag<T> forRemote(NamedRemote remote) {
        return forRemote(remote.remoteChannelName());
    }

    @Override
    public void register(ServiceManager manager, T service) {
        throw new UnsupportedOperationException("Can not register remote service tag locally");
    }

    @Override
    public T create(ServiceManager manager) {
        RPCService rpcService = manager.getService(RPCService.class);
        return rpcService.bindRemote(rpcService.communicationProvider.channel(remoteName), getServiceClass());
    }

}
