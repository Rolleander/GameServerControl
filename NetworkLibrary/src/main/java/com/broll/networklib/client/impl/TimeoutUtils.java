package com.broll.networklib.client.impl;

import com.esotericsoftware.minlog.Log;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class TimeoutUtils {

    public final static int TIMEOUT = 5;

    public static <T> void scheduleTimeout(CompletableFuture<T> future, Consumer<CompletableFuture<T>> timedout) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
                    if (!future.isDone()) {
                        Log.warn("Request timed out");
                        timedout.accept(future);
                    }
                }
                , TIMEOUT, TimeUnit.SECONDS);
        executor.shutdown();
    }
}
