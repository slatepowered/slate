package slatepowered.slate.model;

/**
 * Represents any object (node, resource, etc) bound to
 * a network.
 */
public interface NetworkObject {

    /**
     * Get the network this object is bound to.
     *
     * @return The network.
     */
    Network getNetwork();

}
