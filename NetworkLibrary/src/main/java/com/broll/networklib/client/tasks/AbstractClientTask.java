package com.broll.networklib.client.tasks;

import com.broll.networklib.client.ClientSite;
import com.broll.networklib.client.GameClient;
import com.broll.networklib.client.LobbyClientSite;
import com.broll.networklib.client.LobbyGameClient;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.network.NetworkException;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractClientTask<T> {

    private CompletableFuture<T> future;
    private LobbyGameClient client;
    private List<Runnable> onComplete = new ArrayList<>();
    private IRegisterNetwork networkRegister;
    private final static int TIMEOUT = 5;

    public CompletableFuture<T> run(LobbyGameClient client, IRegisterNetwork networkRegister) {
        this.networkRegister = networkRegister;
        this.client = client;
        this.future = new CompletableFuture<>();
        try {
            run();
        } catch (Exception e) {
            failure(e);
        }
        return future;
    }

    protected abstract void run();

    protected void assureConnected() {
        if (!client.isConnected()) {
            throw new NetworkException("Client must be connected for this task");
        }
    }

    protected void runOnConnectedClient(LobbyClientSite site) {
        assureConnected();
        client.register(site);
        onComplete.add(() -> client.unregister(site));
    }

    protected void runOnClient(String ip, LobbyClientSite site) {
        if (client.isConnected()) {
            if (!StringUtils.equals(client.getConnectedIp(), ip)) {
                connect(client, ip);
            }
        } else {
            connect(client, ip);
        }
        client.register(site);
        onComplete.add(() -> client.unregister(site));
    }

    protected void runOnTempClient(String ip, ClientSite site) {
        GameClient gameClient = new GameClient(networkRegister);
        gameClient.connect(ip);
        gameClient.register(site);
        onComplete.add(() -> gameClient.shutdown());
    }

    protected <F> F waitFor(Future<F> future) {
        try {
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new NetworkException(e);
        }
    }

    private void connect(LobbyGameClient client, String ip) {
        client.connectToServer(ip);
    }

    protected void complete(T result) {
        onComplete.forEach(Runnable::run);
        future.complete(result);
    }

    protected void failure(Exception e) {
        onComplete.forEach(Runnable::run);
        future.completeExceptionally(e);
    }


}
