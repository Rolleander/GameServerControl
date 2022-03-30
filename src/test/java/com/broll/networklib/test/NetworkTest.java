package com.broll.networklib.test;

import com.broll.networklib.client.auth.ClientAuthenticationKey;
import com.broll.networklib.client.LobbyGameClient;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.LobbyGameServer;
import com.broll.networklib.server.NetworkConnection;
import com.broll.networklib.server.impl.ILobbyData;
import com.broll.networklib.server.impl.ServerLobby;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class NetworkTest<L extends ILobbyData, P extends ILobbyData> {
    private final static Logger Log = LoggerFactory.getLogger(NetworkTest.class);

    private final static int TIMEOUT = 5000;
    private final static String IP = "localhost";
    protected TestServer server;
    protected LobbyGameServer<L, P> gameServer;
    protected Map<LobbyGameClient, TestClientData> clients = new HashMap<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    protected abstract IRegisterNetwork registerNetwork();

    @Before
    public void before() {
        server = new TestServer(registerNetwork(), TIMEOUT);
        gameServer = new LobbyGameServer<>(server, "TestServer");
        registerServerSites(gameServer);
        gameServer.open();
    }

    protected void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    @After
    public void after() {
        clients.keySet().forEach(LobbyGameClient::shutdown);
        gameServer.shutdown();
    }

    public abstract void registerServerSites(LobbyGameServer<L, P> server);

    public abstract void registerClientSites(LobbyGameClient client);

    public LobbyGameClient testClient(String name) {
        TestClient testClient = new TestClient(registerNetwork(), TIMEOUT);
        LobbyGameClient client = new LobbyGameClient(testClient);
        registerClientSites(client);
        TestClientData data = new TestClientData();
        data.playerName = name;
        data.testClient = testClient;
        clients.put(client, data);
        client.setClientAuthenticationKey(ClientAuthenticationKey.custom(name));
        client.connectToServer(IP);
        return client;
    }

    public LobbyGameClient testClient(String name, ServerLobby lobbyToConnect) {
        LobbyGameClient client = testClient(name);
        joinLobby(client, lobbyToConnect);
        return client;
    }

    public void dropPackages() {
        dropClientsPackages();
        dropServerPackages();
        sleep();
    }

    public void dropServerPackages() {
        server.dropReceivedPackages();
    }

    public void dropClientsPackages() {
        clients.values().forEach(e -> e.testClient.dropReceivedPackages());
    }

    public <T> T assertClientReceived(LobbyGameClient client, Class<T> type) {
        return clients.get(client).testClient.assureReceived(type);
    }

    public <T> T assertServerReceived(Class<T> type, NetworkConnection from) {
        return server.assureReceived(type, from);
    }

    public TestClient getTestClient(LobbyGameClient client) {
        return clients.get(client).testClient;
    }

    public ServerLobby<L, P> openGameLobby(L data, String name) {
        sleep();
        ServerLobby<L, P> lobby = gameServer.getLobbyHandler().openLobby(name);
        lobby.setData(data);
        return lobby;
    }

    public void expectFailure(Runnable runnable, String failure) {
        try {
            runnable.run();
            Assert.fail("Expected failure: " + failure);
        } catch (RuntimeException e) {
            Assert.assertEquals(failure, e.getMessage());
        }
    }

    protected <T> T waitFor(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void joinLobby(LobbyGameClient client, ServerLobby serverLobby) {
        String name = clients.get(client).playerName;
        Optional<GameLobby> lobbyOptional = waitFor(client.listLobbies()).getLobbies().stream().filter(lobby -> lobby.getLobbyId() == serverLobby.getId()).findFirst();
        if(lobbyOptional.isPresent()){
            GameLobby lobby = lobbyOptional.get();
            Log.info("Client " + name + " discovered lobby " + lobby.getName());
            connectToLobby(client, lobby);
        }
        else{
            throw new RuntimeException("Lobby not found");
        }
    }

    private void connectToLobby(LobbyGameClient client, GameLobby lobby) {
        String name = clients.get(client).playerName;
        waitFor(client.joinLobby(lobby, name));
        Log.info("Client " + name + " joined lobby " + lobby.getName());
    }

    private class TestClientData {
        public String playerName;
        public TestClient testClient;
    }
}
