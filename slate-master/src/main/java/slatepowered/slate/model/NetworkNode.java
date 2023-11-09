package slatepowered.slate.model;

/**
 * Implementation of {@link Node} on the master instance, with
 * full control.
 */
public class NetworkNode extends Node {

    public NetworkNode(String name, Network network, String[] tags) {
        super(name, network, tags);
    }

}
