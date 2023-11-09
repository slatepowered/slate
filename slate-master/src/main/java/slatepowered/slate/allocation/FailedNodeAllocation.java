package slatepowered.slate.allocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FailedNodeAllocation implements NodeAllocationResult {

    /** The error which occurred. */
    private Exception error;

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public NodeAllocation successful() {
        return null;
    }

    @Override
    public FailedNodeAllocation failed() {
        return this;
    }

}
