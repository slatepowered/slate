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
    }

    public static ClusterDeclareCommunicationKey clusterDeclare() {
        return new ClusterDeclareCommunicationKey();
    }

}
