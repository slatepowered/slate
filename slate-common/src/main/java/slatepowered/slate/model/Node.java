package slatepowered.slate.model;

import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceManager;
import slatepowered.slate.service.ServiceResolver;
import slatepowered.slate.service.ServiceTag;

public abstract class Node implements NetworkObject, ServiceResolver {

    /**
     * The network this node is a part of.
     */
    protected final Network network;

    /**
     * The service manager for this node.
     */
    protected final ServiceManager serviceManager;

    Node(Network network) {
        this.network = network;
        this.serviceManager = new ServiceManager(network.serviceManager());
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public ServiceResolver parentServiceResolver() {
        return network.serviceManager();
    }

    @Override
    public <T extends Service> T getService(ServiceTag<T> tag) throws UnsupportedOperationException {
        T service = serviceManager.getService(tag);
        return service == null ? ServiceResolver.super.getService(tag) : service;
    }

}
