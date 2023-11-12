package slatepowered.slate.allocation;

/**
 * Represents the result of a node allocation.
 */
public interface NodeAllocationResult {

    /**
     * Get whether the allocation was successful.
     *
     * @return Whether it was successful.
     */
    boolean isSuccessful();

    /**
     * Get the successful result (cleaner than casting).
     *
     * @return The successful node allocation.
     */
    NodeAllocation successful();

    /**
     * Get the failed result (cleaner than casting).
     *
     * @return The failed node allocation.
     */
    FailedNodeAllocation failed();

}
