package slatepowered.slate.communication;

/**
 * Represents a communication key.
 */
public class CommunicationKey {

    public static class ClusterDeclareCommunicationKey extends CommunicationKey {
        @Override
        public int hashCode() {
            return 6969;
        }

        private static final ClusterDeclareCommunicationKey KEY = new ClusterDeclareCommunicationKey();
    }

    public static ClusterDeclareCommunicationKey clusterDeclare() {
        return ClusterDeclareCommunicationKey.KEY;
    }

}
