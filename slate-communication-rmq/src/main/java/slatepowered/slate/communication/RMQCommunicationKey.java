package slatepowered.slate.communication;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RMQCommunicationKey extends CommunicationKey {

    /**
     * The exchange name.
     */
    protected String exchangeName;

}
