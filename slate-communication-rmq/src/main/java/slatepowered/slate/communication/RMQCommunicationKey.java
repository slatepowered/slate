package slatepowered.slate.communication;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RMQCommunicationKey extends CommunicationKey {

    public static RMQCommunicationKey named(String exchangeName) {
        return new RMQCommunicationKey(exchangeName);
    }

    /**
     * The exchange name.
     */
    protected String exchangeName;

}
