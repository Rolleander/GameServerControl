package com.broll.networklib.client;

import com.broll.networklib.GameEndpoint;
import com.broll.networklib.NetworkRegister;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.ILobbyDiscovery;
import com.broll.networklib.client.impl.ILobbyReconnect;
import com.broll.networklib.client.impl.LobbyConnectionSite;
import com.broll.networklib.client.impl.LobbyLookupSite;
import com.broll.networklib.client.impl.LobbyReconnectSite;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.network.NetworkException;
import com.broll.networklib.network.INetworkRequestAttempt;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;
import com.google.common.util.concurrent.SettableFuture;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class LobbyGameClient implements NetworkRegister {

    private ExecutorService discoveryExecutor;
    private GameClient client;
    private LobbyConnectionSite lobbyConnectionSite = new LobbyConnectionSite();
    private ClientAuthenticationKey clientAuthenticationKey = ClientAuthenticationKey.fromFileCache();
    private boolean discoveringLobbies = false;

    public LobbyGameClient(IRegisterNetwork registerNetwork) {
        this(new GameClient(registerNetwork));
    }

    public LobbyGameClient(GameClient client) {
        discoveryExecutor = Executors.newSingleThreadExecutor();
        this.client = client;
        register(lobbyConnectionSite);
    }

    public void setClientAuthenticationKey(ClientAuthenticationKey clientAuthenticationKey) {
        this.clientAuthenticationKey = clientAuthenticationKey;
    }

    @Override
    public Kryo getKryo() {
        return client.getKryo();
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

    public Future<Integer> discoverLobbies(ILobbyDiscovery lobbyDiscovery) {
        return discoveryTask(false, () -> {
            List<String> servers = client.discoverServers();
            //also check localhost for servers
            servers.add("localhost");
            //lookup lobbies in each server
            if (servers.isEmpty()) {
                lobbyDiscovery.noLobbiesDiscovered();
                return 0;
            }
            int foundLobbies = servers.stream().map(server -> {
                try {
                    return LobbyLookupSite.openLobbyLookupClient(server, client.getRegisterNetwork(), lobbyDiscovery).get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.error("Interrupted discovery future", e);
                    return 0;
                }
            }).reduce(0, (a, b) -> a + b);
            if (foundLobbies == 0) {
                lobbyDiscovery.noLobbiesDiscovered();
            } else {
                lobbyDiscovery.discoveryDone();
            }
            return foundLobbies;
        });
    }

    public void clearClientAuthenticationKey() {
        ClientAuthenticationKey.clearFileCache();
    }

    public void connectToLobby(GameLobby lobby, String playerName, INetworkRequestAttempt<GameLobby> request) {
        GameEndpoint.attemptRequest(request, () -> {
            client.connect(lobby.getServerIp());
            lobbyConnectionSite.tryJoinLobby(lobby, playerName, clientAuthenticationKey, request);
        });
    }

    public Future<Integer> listLobbies(String ip, ILobbyDiscovery lobbyDiscovery) {
        return discoveryTask(false, () -> {
            try {
                int discoveredLobbies = LobbyLookupSite.openLobbyLookupClient(ip, client.getRegisterNetwork(), lobbyDiscovery).get();
                if (discoveredLobbies == 0) {
                    lobbyDiscovery.noLobbiesDiscovered();
                } else {
                    lobbyDiscovery.discoveryDone();
                }
                return discoveredLobbies;
            } catch (InterruptedException | ExecutionException e) {
                Log.error("Interrupted discovery future", e);
                lobbyDiscovery.noLobbiesDiscovered();
            }
            return 0;
        });
    }

    public Future<Integer> listLobbies(ILobbyDiscovery lobbyDiscovery) {
        return discoveryTask(true, () -> {
            try {
                int discoveredLobbies = LobbyLookupSite.lookupLobbies(client, lobbyDiscovery).get();
                if (discoveredLobbies == 0) {
                    lobbyDiscovery.noLobbiesDiscovered();
                } else {
                    lobbyDiscovery.discoveryDone();
                }
                return discoveredLobbies;
            } catch (InterruptedException | ExecutionException e) {
                Log.error("Interrupted discovery future", e);
                lobbyDiscovery.noLobbiesDiscovered();
            }
            return 0;
        });
    }

    public Future<Boolean> checkForReconnection(String ip, ILobbyReconnect reconnect) {
        return discoveryTask(false, () -> {
            try {
                return LobbyReconnectSite.checkForReconnect(ip, client.getRegisterNetwork(), clientAuthenticationKey, lobby -> {
                    connectToServer(ip);
                    lobbyConnectionSite.reconnectedToLobby(lobby);
                    reconnect.reconnected(lobby);
                }).get();
            } catch (Exception e) {
                Log.error("Interrupted reconnect future", e);
                return false;
            }
        });
    }

    private <T> Future<T> discoveryTask(boolean clientMustBeConnected, Supplier<T> task) {
        if (discoveringLobbies) {
            Log.error("Client ist already busy with a discovery task");
            return CompletableFuture.completedFuture(null);
        }
        discoveringLobbies = true;
        if (clientMustBeConnected && !client.isConnected()) {
            throw new NetworkException("Client must be connected to a server before calling this");
        }
        return CompletableFuture.supplyAsync(task, discoveryExecutor).whenComplete((o, f) -> discoveringLobbies = false);
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
        discoveryExecutor.shutdown();
        client.shutdown();
    }

}

