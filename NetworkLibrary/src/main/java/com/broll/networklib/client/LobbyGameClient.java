package com.broll.networklib.client;

import com.broll.networklib.NetworkRegister;
import com.broll.networklib.client.auth.ClientAuthenticationKey;
import com.broll.networklib.client.auth.LastConnection;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.LobbyConnectionSite;
import com.broll.networklib.client.tasks.AbstractClientTask;
import com.broll.networklib.client.tasks.DiscoveredLobbies;
import com.broll.networklib.client.tasks.impl.CreateLobbyTask;
import com.broll.networklib.client.tasks.impl.JoinLobbyTask;
import com.broll.networklib.client.tasks.impl.LobbyDiscoveryTask;
import com.broll.networklib.client.tasks.impl.LobbyListTask;
import com.broll.networklib.client.tasks.impl.ReconnectTask;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.network.NetworkException;
import com.broll.networklib.site.SiteReceiver;
import com.esotericsoftware.kryo.Kryo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LobbyGameClient implements NetworkRegister {

    private ExecutorService taskExecutor;
    private GameClient client;
    private LobbyConnectionSite lobbyConnectionSite = new LobbyConnectionSite(() -> connectedLobby = null);
    private ClientAuthenticationKey clientAuthenticationKey = ClientAuthenticationKey.fromFileCache();
    private GameLobby connectedLobby;

    public LobbyGameClient(IRegisterNetwork registerNetwork) {
        this(new GameClient(registerNetwork));
    }

    public LobbyGameClient(GameClient client) {
        taskExecutor = Executors.newSingleThreadExecutor();
        this.client = client;
        register(lobbyConnectionSite);
    }

    public void setSiteReceiver(SiteReceiver<ClientSite, GameClient.ClientConnection> receiver) {
        client.setSiteReceiver(receiver);
    }

    public void setClientAuthenticationKey(ClientAuthenticationKey clientAuthenticationKey) {
        this.clientAuthenticationKey = clientAuthenticationKey;
    }

    public void sendTCP(Object message) {
        client.sendTCP(message);
    }

    public void sendUDP(Object message) {
        client.sendUDP(message);
    }

    @Override
    public Kryo getKryo() {
        return client.getKryo();
    }

    @Override
    public void registerNetworkPackage(String packagePath) {
        client.registerNetworkPackage(packagePath);
    }

    @Override
    public void registerNetworkType(Class type) {
        client.registerNetworkType(type);
    }

    public void register(LobbyClientSite... sites) {
        client.register(sites);
        Arrays.stream(sites).forEach(site -> site.init(this));
    }

    public void unregister(LobbyClientSite... sites) {
        client.unregister(sites);
    }

    public void clearSites() {
        client.clearSites();
    }

    private <T> CompletableFuture<T> runTask(AbstractClientTask<T> task) {
        return CompletableFuture.supplyAsync(() ->
                        task.run(this, client.getRegisterNetwork())
                , taskExecutor).thenCompose(f -> f);
    }

    private CompletableFuture<GameLobby> updateLobby(CompletableFuture<GameLobby> future) {
        return future.whenComplete((lobby, e) -> {
            if (e != null) {
                lobby = null;
            }
            this.connectedLobby = lobby;
            this.lobbyConnectionSite.setLobby(lobby);
        });
    }

    public CompletableFuture<GameLobby> reconnectCheck() {
        String ip = LastConnection.getLastConnection();
        if (ip == null) {
            CompletableFuture<GameLobby> future = new CompletableFuture<>();
            future.complete(null);
            return future;
        }
        return reconnectCheck(ip);
    }

    public CompletableFuture<GameLobby> reconnectCheck(String ip) {
        return updateLobby(runTask(new ReconnectTask(ip, clientAuthenticationKey)));
    }

    public CompletableFuture<List<DiscoveredLobbies>> discoverLobbies() {
        return runTask(new LobbyDiscoveryTask(client));
    }

    public CompletableFuture<DiscoveredLobbies> listLobbies(String ip) {
        return runTask(new LobbyListTask(ip));
    }

    public CompletableFuture<DiscoveredLobbies> listLobbies() {
        return runTask(new LobbyListTask());
    }

    public CompletableFuture<GameLobby> joinLobby(GameLobby lobby, String playerName) {
        return updateLobby(runTask(new JoinLobbyTask(lobby, playerName, clientAuthenticationKey)));
    }

    public CompletableFuture<GameLobby> createLobby(String playerName, Object lobbySettings) {
        return updateLobby(runTask(new CreateLobbyTask(playerName, lobbySettings, clientAuthenticationKey)));
    }

    public void clearClientAuthenticationKey() {
        ClientAuthenticationKey.clearFileCache();
    }

    public GameLobby getConnectedLobby() {
        return connectedLobby;
    }

    public boolean isInLobby() {
        return connectedLobby != null;
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public String getConnectedIp() {
        return client.getConnectedIp();
    }

    public void shutdown() {
        taskExecutor.shutdown();
        client.shutdown();
    }

    public void connectToServer(String ip) {
        client.connect(ip);
    }
}

