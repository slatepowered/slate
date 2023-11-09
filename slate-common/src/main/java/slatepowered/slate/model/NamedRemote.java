package slatepowered.slate.model;

import slatepowered.reco.Channel;

public interface NamedRemote {

    /**
     * The name of this remote object to be used as
     * the target for a communication channel.
     *
     * @return The name of the remote.
     */
    String remoteChannelName();

    /**
     * Get the channel connecting to this remote.
     *
     * @return The channel.
     */
    Channel getChannel();

}
