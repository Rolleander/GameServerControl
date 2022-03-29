package com.broll.networklib.client.tasks;

import com.broll.networklib.client.LobbyClientSite;
import com.broll.networklib.network.NetworkException;

import java.util.concurrent.CompletableFuture;

public class AbstractTaskSite<T> extends LobbyClientSite {

    private CompletableFuture<T> future = new CompletableFuture<>();

    protected void complete(T t) {
        future.complete(t);
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    protected void fail(String reason) {
        future.completeExceptionally(new Exception(reason));
    }

    @Override
    public void onDisconnect() {
        future.completeExceptionally(new NetworkException("Connection lost"));
    }
}
