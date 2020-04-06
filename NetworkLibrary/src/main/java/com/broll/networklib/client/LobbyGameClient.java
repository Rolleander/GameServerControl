package com.broll.networklib.client;

import com.broll.networklib.GameEndpoint;
import com.broll.networklib.NetworkRegister;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.ILobbyDiscovery;
import com.broll.networklib.client.impl.LobbyConnectionSite;
import com.broll.networklib.client.impl.LobbyLookupSite;
import com.broll.networklib.network.NetworkDiscovery;
import com.broll.networklib.network.NetworkException;
import com.broll.networklib.network.INetworkRequestAttempt;
import com.esotericsoftware.minlog.Log;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LobbyGameClient implements NetworkRegister {

    private ExecutorService discoveryExecutor;
    private GameClient client;
    private LobbyConnectionSite lobbyConnectionSite = new LobbyConnectionSite();

    public LobbyGameClient() {
        this(new GameClient());
    }

    public LobbyGameClient(GameClient client) {
        discoveryExecutor = Executors.newSingleThreadExecutor();
        this.client = client;
        register(lobbyConnectionSite);
    }

    public void connectToServer(String ip) {
        client.connect(ip);
    }

    @Override
    public void registerNetworkPackage(String packagePath) {
        client.registerNetworkPackage(packagePath);
    }

    @Override
    public void registerNetworkType(Class type) {
        client.registerNetworkType(type);
    }

    public void register(ClientSite... sites) {
        client.register(sites);
    }

    public void discoverLobbies(ILobbyDiscovery lobbyDiscovery) {
        discoveryExecutor.execute(() -> {
            List<String> servers = client.discoverServers();
            //also check localhost for servers
            servers.add("localhost");
            //lookup lobbies in each server
            if (servers.isEmpty()) {
                lobbyDiscovery.noLobbiesDiscovered();
                return;
            }
            int foundLobbies = servers.stream().map(server -> {
                try {
                    return LobbyLookupSite.openLobbyLookupClient(server, lobbyDiscovery).get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.error("Interrupted discovery future", e);
                    return 0;
                }
            }).reduce(0, (a, b) -> a + b);
            if (foundLobbies == 0) {
                lobbyDiscovery.noLobbiesDiscovered();
            }
        });
    }

    public void clearClientAuthenticationKey() {
        ClientAuthenticationKey.clearFileCache();
    }

    public void connectToLobby(GameLobby lobby, String playerName, INetworkRequestAttempt<GameLobby> request) {
        connectToLobby(lobby, playerName, request,  ClientAuthenticationKey.fromFileCache());
    }

    public void connectToLobby(GameLobby lobby, String playerName, INetworkRequestAttempt<GameLobby> request, ClientAuthenticationKey key) {
        GameEndpoint.attemptRequest(request, () -> {
            client.connect(lobby.getServerIp());
            lobbyConnectionSite.tryJoinLobby(lobby, playerName, key, request);
        });
    }

    public void listLobbies(String ip, ILobbyDiscovery lobbyDiscovery) {
        try {
            int discoveredLobbies = LobbyLookupSite.openLobbyLookupClient(ip, lobbyDiscovery).get();
            if (discoveredLobbies == 0) {
                lobbyDiscovery.noLobbiesDiscovered();
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.error("Interrupted discovery future", e);
            lobbyDiscovery.noLobbiesDiscovered();
        }
    }

    public void listLobbies(ILobbyDiscovery lobbyDiscovery){
        if(!client.isConnected()){
            throw new NetworkException("Cannot list lobbies when not connected to a server");
        }
        try {
            int discoveredLobbies = LobbyLookupSite.lookupLobbies(client, lobbyDiscovery).get();
            if (discoveredLobbies == 0) {
                lobbyDiscovery.noLobbiesDiscovered();
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.error("Interrupted discovery future", e);
            lobbyDiscovery.noLobbiesDiscovered();
        }
    }

    public void createLobby(String playerName, Object settings, INetworkRequestAttempt<GameLobby> request) {
        if (!client.isConnected()) {
            throw new NetworkException("Cannot create lobby when not connected to a server");
        }
        GameEndpoint.attemptRequest(request, () -> {
            lobbyConnectionSite.tryCreateLobby(playerName, settings, ClientAuthenticationKey.fromFileCache(), request);
        });
    }

    public void shutdown() {
        client.shutdown();
    }


}

