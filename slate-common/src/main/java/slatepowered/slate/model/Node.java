package slatepowered.slate.model;

import slatepowered.reco.Channel;
import slatepowered.slate.security.SecurityObject;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceKey;
import slatepowered.slate.service.ServiceManager;
import slatepowered.slate.service.ServiceProvider;
import slatepowered.slate.service.network.NodeBoundServiceKey;
import slatepowered.slate.service.network.NodeHostBoundServiceKey;
import slatepowered.slate.service.remote.RemoteServiceKey;

/**
 * Represents a node in the network which is built on
 * components and services.
 */
public abstract class Node implements NetworkObject, ServiceProvider, SecurityObject, NamedRemote {

    /**
     * The name of this node.
     */
    protected final String name;

    /**
     * The network this node is a part of.
     */
    protected final Network network;

    /**
     * The service manager for this node.
     */
    protected final ServiceManager serviceManager;

    public Node(String name, Network network) {
        this.name = name;
        this.network = network;
        this.serviceManager = new ServiceManager(network.serviceManager());
    }

    @Override
    public ServiceManager serviceManager() {
        return serviceManager;
    }

    public String getName() {
        return name;
    }

    /**
     * Get the network this node is bound to.
     *
     * @return The network instance.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <N2 extends Network> N2 getNetwork() {
        return (N2) network;
    }

    /**
     * Get the tags applied to this node.
     *
     * @return The tags.
     */
    public abstract String[] getTags();

    /* SecurityObject impl */

    @Override
    public String[] getSecurityGroups() {
        return getTags();
    }

    /* ServiceResolver impl */

    @Override
    public <T extends Service> ServiceKey<T> qualifyServiceKey(ServiceKey<T> key) throws UnsupportedOperationException {
        if (key instanceof NodeBoundServiceKey) {
            ((NodeBoundServiceKey)key).forNode(this);
        } else if (key instanceof RemoteServiceKey) {
            ((RemoteServiceKey<?>)key).forRemote(this);
        }

        return key;
    }

    @Override
    public ServiceProvider parentServiceResolver() {
        return network.serviceManager();
    }

    @Override
    public <T extends Service> T getService(ServiceKey<T> tag) throws UnsupportedOperationException {
        T service = serviceManager.getService(tag, this);
        return service == null ? ServiceProvider.super.getService(tag) : service;
    }

    @Override
    public String remoteChannelName() {
        return name;
    }

    @Override
    public Channel getChannel() {
        return network.getCommunicationProvider().channel(remoteChannelName());
    }

    //////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public static Node masterNode(Network network) {
        return new Node("master", network) {
            // the tags on this node
            final String[] tags = new String[] { "*", "master" };

            @Override
            public String[] getTags() {
                return tags;
            }

            @Override
            public String remoteChannelName() {
                return "master";
            }
        };
    }

}
