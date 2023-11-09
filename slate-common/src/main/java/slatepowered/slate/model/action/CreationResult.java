package slatepowered.slate.model.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreationResult {
    /** Whether the creation was successful. */
    private boolean success;

    /** The errors/warnings returned by the creation result. */
    private List<Throwable> errors;
}
