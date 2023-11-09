package slatepowered.slate.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.network.NetworkServiceKey;
import slatepowered.veru.data.Pair;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * A service provided by the master which exposes data about the
 * network and nodes inside it.
 */
public interface NetworkInfoService extends Service, RemoteAPI {

    NetworkServiceKey<NetworkInfoService> KEY = NetworkServiceKey.provided(NetworkInfoService.class, "master");

    static NetworkServiceKey<NetworkInfoService> key() {
        return KEY;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class NodeInfo {
        private String name;    // The name of the node
        private String parent;  // The parent node of the node
        private String[] tags;  // The tags/security groups on this node
        private String cluster; // The cluster the node is allocated on
    }

    /**
     * Fetches the names and tags of all nodes on the network.
     *
     * @return The names of all the nodes on the network.
     */
    Collection<Pair<String, String[]>> fetchNodeNames();

    default CompletableFuture<Collection<Pair<String, String[]>>> fetchNodeNamesAsync() {
        return null;
    }

    /**
     * Fetches the information about a node with the given name.
     *
     * @param name The name of the node.
     * @return The information about the node or null if absent.
     */
    NodeInfo fetchNodeInfo(String name);

    default CompletableFuture<NodeInfo> fetchNodeInfoAsync(String name) {
        return null;
    }



}
