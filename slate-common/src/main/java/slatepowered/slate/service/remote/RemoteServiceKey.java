package slatepowered.slate.service.remote;

import slatepowered.reco.rpc.RPCManager;
import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.slate.model.NamedRemote;
import slatepowered.slate.service.DynamicServiceKey;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceManager;
import slatepowered.slate.service.ServiceProvider;

import java.util.Objects;

/**
 * todo
 */
public abstract class RemoteServiceKey<T extends Service & RemoteAPI> implements DynamicServiceKey<T> {

    public static <T extends Service & RemoteAPI> RemoteServiceKey<T> remote(Class<T> tClass) {
        return new RemoteServiceKey<T>() {
            @Override
            public Class<T> getServiceClass() {
                return tClass;
            }
        };
    }

    /** The remote channel name */
    protected String remoteName;

    public RemoteServiceKey<T> forRemote(String remoteName) {
        this.remoteName = remoteName;
        return this;
    }

    public RemoteServiceKey<T> forRemote(NamedRemote remote) {
        return forRemote(remote.remoteChannelName());
    }

    @Override
    public void register(ServiceManager manager, T service) {
        throw new UnsupportedOperationException("Can not register remote service key locally");
    }

    @Override
    public T create(ServiceProvider provider) {
        System.out.println("Service: Remote: binding service to remoteChannel(" + remoteName + ")");
        Objects.requireNonNull(remoteName, "Remote name can not be null");

        RPCManager rpcManager = provider.getSingleton(RPCManager.class);
        Objects.requireNonNull(rpcManager, "Could not find valid RPC manager singleton");
        return rpcManager.bindRemote(rpcManager.getLocalChannel().provider().channel(remoteName), getServiceClass());
    }

}
