package slatepowered.slate.model;

import lombok.Builder;
import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.ProvidedChannel;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.slate.security.NetworkRPCSecurityManager;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceManager;
import slatepowered.slate.service.ServiceResolver;
import slatepowered.slate.service.ServiceTag;
import slatepowered.slate.service.remote.RPCService;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Slate network.
 */
@Builder
public abstract class Network implements ServiceResolver {

    /**
     * The communication provider.
     */
    protected final CommunicationProvider<ProvidedChannel> communicationProvider;

    /**
     * The remote procedure call manager.
     */
    protected final RPCService rpcManager;

    /**
     * The service manager for this network.
     */
    protected final ServiceManager serviceManager;

    /**
     * The virtual master node.
     */
    protected final Node master;

    /**
     * All nodes by name.
     */
    protected final Map<String, Node> nodeMap = new HashMap<>();

    protected Network(CommunicationProvider<ProvidedChannel> communicationProvider,
                      Node master) {
        this.communicationProvider = communicationProvider;
        this.master = master;

        this.rpcManager = new RPCService(communicationProvider);
        rpcManager.setInboundSecurityManager(new NetworkRPCSecurityManager(this));
        this.serviceManager = new ServiceManager()
                .register(RPCService.TAG, rpcManager);
    }

    /**
     * Get the network's local service manager.
     *
     * @return The service manager.
     */
    public ServiceManager serviceManager() {
        return serviceManager;
    }

    /**
     * Get the master node information.
     *
     * @return The master.
     */
    public Node master() {
        return master;
    }

    /**
     * Get a registered node by name.
     *
     * @param name The name.
     * @return The node or null if absent.
     */
    public Node getNode(String name) {
        return nodeMap.get(name);
    }

    /* ServiceResolver impl */

    @Override
    public <T extends Service> ServiceTag<T> qualifyServiceTag(ServiceTag<T> tag) throws UnsupportedOperationException {
        return tag;
    }

    @Override
    public ServiceResolver parentServiceResolver() {
        return serviceManager();
    }

}
