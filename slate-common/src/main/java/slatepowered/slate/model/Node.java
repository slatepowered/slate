package slatepowered.slate.model;

import slatepowered.reco.Channel;
import slatepowered.veru.collection.Subset;
import slatepowered.slate.security.SecurityObject;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceManager;
import slatepowered.slate.service.ServiceProvider;
import slatepowered.slate.service.ServiceKey;
import slatepowered.slate.service.network.NodeBoundServiceKey;
import slatepowered.slate.service.remote.RemoteServiceKey;
import slatepowered.veru.collection.ArrayUtil;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * The security groups for this node.
     */
    protected final String[] tags;

    /**
     * All components attached to this node.
     */
    protected final List<NodeComponent> components = new ArrayList<>();

    public Node(String name, Network network, String[] tags) {
        this.name = name;
        this.network = network;
        this.serviceManager = new ServiceManager(network.serviceManager());
        this.tags = ArrayUtil.concat(new String[] { "node" }, tags);
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
    public Network getNetwork() {
        return network;
    }

    /**
     * Get the tags applied to this node.
     *
     * @return The tags.
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * Get all components attached to this node.
     *
     * @return The components.
     */
    public List<NodeComponent> getComponents() {
        return components;
    }

    /**
     * Find all components which are assignable to the given class.
     *
     * @param kl The class.
     * @param <T> The value type.
     * @return The list of components.
     */
    @SuppressWarnings("unchecked")
    public <T extends NodeComponent> Subset<T> findComponents(Class<T> kl) {
        return (Subset<T>) Subset.filter(components, c -> kl.isAssignableFrom(c.getClass()));
    }

    /* SecurityObject impl */

    @Override
    public String[] getSecurityGroups() {
        return tags;
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
        T service = serviceManager.getService(tag);
        return service == null ? ServiceProvider.super.getService(tag) : service;
    }

    @Override
    public String remoteChannelName() {
        return "node." + name;
    }

    @Override
    public Channel getChannel() {
        return network.getCommunicationProvider().channel(remoteChannelName());
    }

    /**
     * Create a virtual node representing the master.
     *
     * @return The master node.
     */
    public static Node remoteMaster(Network network) {
        return new Node("master", network, new String[] { "master", "*" }) {
            @Override
            public String remoteChannelName() {
                return "master";
            }
        };
    }

}
