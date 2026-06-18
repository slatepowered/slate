package com.orbyfied.slate.util.logic;

public class Flags {

    public static int toggle(int v, int mask, boolean b) {
        return b ? v | mask : v & ~mask;
    }

}
