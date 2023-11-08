package example.service.api;

import lombok.Data;

@Data
public class NodeCommandEvent {

    private final String node;
    private final String command;

}
