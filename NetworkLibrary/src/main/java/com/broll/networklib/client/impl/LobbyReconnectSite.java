package com.broll.networklib.client.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.ClientAuthenticationKey;
import com.broll.networklib.client.ClientSite;
import com.broll.networklib.client.GameClient;
import com.broll.networklib.client.LobbyClientSite;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.network.nt.NT_LobbyJoined;
import com.broll.networklib.network.nt.NT_LobbyNoJoin;
import com.broll.networklib.network.nt.NT_ReconnectCheck;
import com.esotericsoftware.minlog.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LobbyReconnectSite extends LobbyClientSite {

    private ILobbyReconnect reconnectCheck;

    private CompletableFuture<Boolean> discoveryFuture;

    private final static int TIMEOUT = 5;

    public LobbyReconnectSite(ILobbyReconnect reconnectCheck) {
        this.reconnectCheck = reconnectCheck;
        this.discoveryFuture = new CompletableFuture<>();
    }

    public static CompletableFuture<Boolean> checkForReconnect(String ip, GameClient client, ClientAuthenticationKey authenticationKey, ILobbyReconnect reconnectCheck) {
        LobbyReconnectSite lookupSite = new LobbyReconnectSite(reconnectCheck);
        client.register(lookupSite);
        NT_ReconnectCheck reconnect = new NT_ReconnectCheck();
        reconnect.authenticationKey = authenticationKey.getSecret();
        client.connect(ip);
        client.sendTCP(reconnect);
        lookupSite.scheduleTimeout();
        return lookupSite.discoveryFuture.whenComplete((r, t) -> client.unregister(lookupSite));
    }

    private void scheduleTimeout() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
                    if (!discoveryFuture.isDone()) {
                        Log.warn("Reconnect check timed out on server " + this.getClient().getConnectedIp());
                        discoveryFuture.complete(false);
                    }
                }
                , TIMEOUT, TimeUnit.SECONDS);
        executor.shutdown();
    }

    @Override
    public void onDisconnect() {
        if (!discoveryFuture.isDone()) {
            Log.warn("Reconnect check client disconnected");
            discoveryFuture.complete(false);
        }
    }

    @PackageReceiver
    public void reconnected(NT_LobbyJoined reconnected) {
        GameLobby lobby = new GameLobby();
        lobby.setSettings(reconnected.settings);
        lobby.setLobbyId(reconnected.lobbyId);
        lobby.setName(reconnected.lobbyName);
        lobby.setPlayerCount(reconnected.playerCount);
        lobby.setPlayerLimit(reconnected.playerLimit);
        lobby.setServerIp(getClient().getConnectedIp());
        Map<Integer, LobbyPlayer> players = new HashMap<>();
        Arrays.stream(reconnected.players).forEach(player -> {
            LobbyPlayer lp = new LobbyPlayer(player.id, lobby);
            lp.setName(player.name);
            lp.setSettings(player.settings);
            players.put(player.id, lp);
        });
        lobby.setPlayers(players);
        lobby.playerJoined(reconnected.playerId);
        reconnectCheck.reconnected(lobby);
        discoveryFuture.complete(true);
    }

    @PackageReceiver
    public void notReconnected(NT_LobbyNoJoin notReconnected) {
        discoveryFuture.complete(false);
    }
}