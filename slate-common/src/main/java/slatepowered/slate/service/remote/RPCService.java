package slatepowered.slate.service.remote;

import slatepowered.reco.Channel;
import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.ProvidedChannel;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceTag;

/**
 * Provides an {@link RPCManager} to all services
 * through the service manager.
 */
public class RPCService extends RPCManager implements Service {

    /**
     * The communication provider.
     */
    protected final CommunicationProvider<?> communicationProvider;

    public static final ServiceTag<RPCService> TAG = ServiceTag.local(RPCService.class);

    public RPCService(ProvidedChannel localChannel) {
        super(localChannel);
        this.communicationProvider = localChannel.provider();
    }

    public CommunicationProvider<?> getCommunicationProvider() {
        return communicationProvider;
    }

}
