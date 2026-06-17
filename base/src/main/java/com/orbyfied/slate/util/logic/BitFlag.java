package com.orbyfied.slate.util.logic;

import java.util.BitSet;

/**
 * Represents a flag object, which can
 * be stored in a bit set (integer as well).
 */
public interface BitFlag {

    /**
     * Create a new basic bit flag with
     * a specific bit offset set.
     *
     * @param offset The bit offset.
     * @return The flag instance.
     */
    static BitFlag at(final int offset) {
        return () -> offset;
    }

    /**
     * Create a new bit flag which will produce
     * a full bit mask to cover all bits. Bit
     * offset is set to {@code -1}.
     *
     * @return The flag instance.
     */
    static BitFlag all() {
        // return new instance
        return new BitFlag() {
            @Override
            public int getBitOffset() {
                return -1;
            }

            @Override
            public long getBitMask() {
                return 0xffffffffffffffffL;
            }

            @Override
            public BitSet set(BitSet set, boolean value) {
                set.set(0, set.length(), value);
                return set;
            }
        };
    }

    ////////////////////////////////////

    /**
     * Get the offset of the bit this
     * flag uses in a bit set.
     * A value of {@code -1} will enable
     * everything.
     *
     * @return The offset.
     */
    int getBitOffset();

    /**
     * Get the bit mask by shifting one
     * {@link BitFlag#getBitOffset()} times
     * to the right. Will overflow and produce
     * incorrect results if the bit offset is
     * over 64 (max {@code long} capacity). Then
     * you will have to use a {@link BitSet} with
     * {@link BitFlag#set(BitSet, boolean)}
     * instead of primitives.
     *
     * @return The bit mask.
     */
    default long getBitMask() {
        return 1L >> getBitOffset();
    }

    /**
     * Set the flag bit to the given value
     * in the provided bit set. Modifies
     * the input bit set instance.
     *
     * @param set The bit set.
     * @param value True/false.
     * @return The same bit set instance.
     */
    default BitSet set(BitSet set, boolean value) {
        set.set(getBitOffset(), value);
        return set;
    }

    /**
     * Set the flag bit to the given value
     * in the provided integer. Returns a
     * new result value.
     *
     * @param i The input integer.
     * @param value True/false.
     * @return The output integer.
     */
    default int set(int i, boolean value) {
        if (value) return i | (int)getBitMask();
        else return i & ~(int)getBitMask();
    }

    /**
     * Set the flag bit to the given value
     * in the provided integer. Returns a
     * new result value.
     *
     * @param i The input integer.
     * @param value True/false.
     * @return The output integer.
     */
    default long set(long i, boolean value) {
        if (value) return i | getBitMask();
        else return i & ~getBitMask();
    }

}
