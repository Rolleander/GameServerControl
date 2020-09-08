package com.broll.networklib.client.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.ClientSite;
import com.broll.networklib.client.GameClient;
import com.broll.networklib.client.LobbyClientSite;
import com.broll.networklib.network.INetworkRequest;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.network.NetworkException;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_ServerInformation;
import com.esotericsoftware.minlog.Log;
import com.google.common.util.concurrent.SettableFuture;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LobbyLookupSite extends LobbyClientSite {

    private ILobbyDiscovery lobbyDiscovery;

    private CompletableFuture<Integer> discoveryFuture;

    private final static int TIMEOUT = 5;

    public LobbyLookupSite(ILobbyDiscovery lobbyDiscovery) {
        this.lobbyDiscovery = lobbyDiscovery;
        this.discoveryFuture = new CompletableFuture<>();
    }

    public static CompletableFuture<Integer> openLobbyLookupClient(String ip, IRegisterNetwork registerNetwork, ILobbyDiscovery lobbyDiscovery) {
        LobbyLookupSite lookupSite = new LobbyLookupSite(lobbyDiscovery);
        GameClient lookupClient = new GameClient(registerNetwork);
        try {
            lookupClient.register(lookupSite);
            lookupClient.connect(ip);
            //request server info
            Log.info("connected to " + lookupClient.getConnectedIp());
            lookupClient.sendTCP(new NT_ServerInformation());
            lookupSite.scheduleTimeout();
        } catch (Exception e) {
            Log.error("Failed to open lobby lookup client on ip " + ip);
            lookupSite.discoveryFuture.complete(0);
        }
       return lookupSite.discoveryFuture.whenComplete((r, t) -> lookupClient.shutdown());
    }

    public static CompletableFuture<Integer> lookupLobbies(GameClient client, ILobbyDiscovery lobbyDiscovery) {
        LobbyLookupSite lookupSite = new LobbyLookupSite(lobbyDiscovery);
        client.register(lookupSite);
        //request server info
        client.sendTCP(new NT_ServerInformation());
        lookupSite.scheduleTimeout();
        return lookupSite.discoveryFuture.whenComplete((r, t) -> client.unregister(lookupSite));
    }

    private void scheduleTimeout() {
        TimeoutUtils.scheduleTimeout(discoveryFuture,future->future.complete(0));
    }

    @Override
    public void onDisconnect() {
        if (!discoveryFuture.isDone()) {
            Log.warn("Lobby discovery client disconnected");
            discoveryFuture.complete(0);
        }
    }

    @PackageReceiver
    public void receive(NT_ServerInformation info) {
        String ip = getClient().getConnectedIp();
        List<GameLobby> lobbies = Arrays.stream(info.lobbies).map(lobbyInfo -> {
            GameLobby lobby = new GameLobby();
            lobby.setServerIp(ip);
            updateLobbyInfo(lobby, lobbyInfo);
            return lobby;
        }).collect(Collectors.toList());
        lobbyDiscovery.discovered(ip, info.serverName, lobbies);
        discoveryFuture.complete(info.lobbies.length);
    }

    public static void updateLobbyInfo(GameLobby lobby, NT_LobbyInformation lobbyInfo) {
        lobby.setName(lobbyInfo.lobbyName);
        lobby.setLobbyId(lobbyInfo.lobbyId);
        lobby.setPlayerCount(lobbyInfo.playerCount);
        lobby.setPlayerLimit(lobbyInfo.playerLimit);
        lobby.setSettings(lobbyInfo.settings);
    }
}