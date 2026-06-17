package com.orbyfied.slate.util.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Async {

    public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(4);
}
