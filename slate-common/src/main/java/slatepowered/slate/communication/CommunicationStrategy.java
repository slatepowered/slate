package slatepowered.slate.communication;

import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.slate.model.Network;

/**
 * Implements communication between and inside a network.
 *
 * @param <K> The communication key.
 */
public abstract class CommunicationStrategy<K extends CommunicationKey> {

    /**
     * Creates a communication provider bound to the given communication key.
     *
     * @param key The communication key.
     * @return The communication provider.
     */
    public abstract CommunicationProvider<?> createCommunicationProvider(K key) throws Exception;

    public RPCManager createRPCManager(K key) throws Exception {
        return new RPCManager(createCommunicationProvider(key));
    }

    /**
     * Get a consistent communication key for the given network.
     *
     * @param network The network.
     * @return The communication key.
     */
    public abstract K getKey(Network<?> network);

}
