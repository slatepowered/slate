package slatepowered.slate.communication;

import slatepowered.reco.CommunicationProvider;
import slatepowered.reco.rpc.RPCManager;
import slatepowered.slate.model.Network;
import slatepowered.veru.misc.Throwables;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements communication between and inside a network.
 */
public abstract class CommunicationStrategy {

    protected String localName;

    /* Caches */
    protected final Map<CommunicationKey, CommunicationProvider<?>> cachedProviders = new HashMap<>();
    protected final Map<CommunicationKey, RPCManager> cachedRPCManagers = new HashMap<>();

    public CommunicationStrategy localName(String localName) {
        this.localName = localName;
        return this;
    }

    /**
     * Gets or creates a communication provider bound to the given communication key.
     *
     * @param key The communication key.
     * @return The communication provider.
     */
    public CommunicationProvider<?> getCommunicationProvider(CommunicationKey key) {
        return cachedProviders.computeIfAbsent(key, __ -> {
            try {
                return createCommunicationProvider(key);
            } catch (Throwable t) {
                Throwables.sneakyThrow(t);
                throw new AssertionError();
            }
        });
    }

    /**
     * Creates a communication provider bound to the given communication key.
     *
     * @param key The communication key.
     * @return The communication provider.
     */
    protected abstract CommunicationProvider<?> createCommunicationProvider(CommunicationKey key) throws Exception;

    /**
     * Create a new, random communication key.
     *
     * @return The key.
     */
    public abstract CommunicationKey createKey();

    /**
     * Get or create an RPC manager for the given communication key.
     *
     * @param key The key.
     * @return The RPC manager.
     */
    public RPCManager getRPCManager(CommunicationKey key) {
        return cachedRPCManagers.computeIfAbsent(key, __ -> new RPCManager(getCommunicationProvider(key)));
    }

}
