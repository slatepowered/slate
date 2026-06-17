package com.orbyfied.slate.util.data;

public class Flags {

    public static int toggle(int v, int mask, boolean b) {
        return b ? v | mask : v & ~mask;
    }

}
