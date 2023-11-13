package slatepowered.slate.cluster;

import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.slate.communication.CommunicationKey;

/**
 * API to communicate with a cluster (not cluster instance).
 */
public interface ClusterInstantiationAPI extends RemoteAPI {

    /**
     * Declares a cluster instance for this network.
     *
     * @param communicationKey The communication key.
     */
    void declareClusterInstance(CommunicationKey communicationKey);

}
