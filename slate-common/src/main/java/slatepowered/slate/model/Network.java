package slatepowered.slate.model;

import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.ProvidedChannel;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.slate.communication.CommunicationKey;
import slatepowered.slate.communication.CommunicationStrategy;
import slatepowered.slate.network.NetworkInfoService;
import slatepowered.slate.plugin.SlatePluginManager;
import slatepowered.slate.security.NetworkRPCSecurityManager;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceManager;
import slatepowered.slate.service.ServiceProvider;
import slatepowered.slate.service.ServiceKey;
import slatepowered.veru.functional.Callback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a Slate network.
 */
public abstract class Network implements ServiceProvider {

    /**
     * The communication strategy.
     */
    protected final CommunicationStrategy communicationStrategy;

    /**
     * The communication key.
     */
    protected final CommunicationKey communicationKey;

    /**
     * The communication provider.
     */
    protected final CommunicationProvider<? extends ProvidedChannel> communicationProvider;

    /**
     * The remote procedure call manager.
     */
    protected final RPCManager rpcManager;

    /**
     * The service manager for this network.
     */
    protected final ServiceManager serviceManager;

    /**
     * All nodes by name.
     */
    protected final Map<String, Node> nodeMap = new ConcurrentHashMap<>();

    /* Events */
    protected final Callback<Void> onCloseEvent = Callback.multi();

    @SuppressWarnings("unchecked")
    public Network(CommunicationKey communicationKey, CommunicationStrategy communicationStrategy) {
        this.communicationKey = communicationKey;

        // register communication strategy and
        // create communication provider for this network
        this.communicationStrategy = communicationStrategy;

        try {
            this.communicationProvider = communicationStrategy.getCommunicationProvider(communicationKey);
        } catch (Throwable t) {
            throw new IllegalStateException("Failed to initialize communication provider", t);
        }

        // create the RPC manager/service
        this.rpcManager = new RPCManager(communicationProvider);
        rpcManager.setInboundSecurityManager(new NetworkRPCSecurityManager(this));

        // create the service manager with
        // default services
        this.serviceManager = new ServiceManager(this)
                .registerSingleton(RPCManager.class, rpcManager);

        getService(NetworkInfoService.KEY).onClose().then(unused -> {
            onCloseEvent.call(null);
            handleOnClose();
            destroy();
        });
    }

    public CommunicationKey getCommunicationKey() {
        return communicationKey;
    }

    public CommunicationStrategy getCommunicationStrategy() {
        return communicationStrategy;
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
     * Get the communication provider used locally.
     *
     * @return The provider.
     */
    public CommunicationProvider<? extends ProvidedChannel> getCommunicationProvider() {
        return communicationProvider;
    }

    /**
     * Get the master node information.
     *
     * @return The master.
     */
    public abstract Node master();

    /**
     * The node representing this local node.
     *
     * @return The local node.
     */
    public abstract ManagedNode local();

    /**
     * Get a pre-registered node by name.
     *
     * This may be null for existent nodes when their data
     * was not fetched from the master.
     *
     * @param name The name.
     * @return The node or null if absent.
     */
    @SuppressWarnings("unchecked")
    public <N extends Node> N getNode(String name) {
        if ("master".equals(name))
            return (N) master();
        return (N) nodeMap.get(name);
    }

    /**
     * Get all cached/registered nodes by name.
     *
     * @return The mutable node map.
     */
    public Map<String, Node> getNodeMap() {
        return nodeMap;
    }

    /**
     * Fetches the node as a {@link Node} object by name,
     * and caches it.
     *
     * @param name The name of the node.
     * @return The node future.
     */
    public CompletableFuture<Node> fetchNode(String name) {
        return CompletableFuture.completedFuture(getNode(name));
    }

    public Network registerNode(Node node) {
        nodeMap.put(node.getName(), node);
        return this;
    }

    // called when the whole network is about to close
    protected void handleOnClose() {

    }

    /**
     * Should be called when this local network instance is
     * closed/destroyed, not only when the actual network is closed.
     */
    public void destroy() {

    }

    /* --------------- Events --------------- */

    public Callback<Void> onClose() {
        return onCloseEvent;
    }

    /* ServiceResolver impl */

    @Override
    public <T extends Service> ServiceKey<T> qualifyServiceKey(ServiceKey<T> tag) throws UnsupportedOperationException {
        return tag;
    }

    @Override
    public ServiceProvider parentServiceResolver() {
        return serviceManager();
    }

}
