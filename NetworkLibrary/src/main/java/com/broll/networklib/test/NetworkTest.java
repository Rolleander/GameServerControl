package com.broll.networklib.test;

import com.broll.networklib.client.ClientAuthenticationKey;
import com.broll.networklib.client.LobbyGameClient;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.ILobbyDiscovery;
import com.broll.networklib.network.INetworkRequestAttempt;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.LobbyGameServer;
import com.broll.networklib.server.NetworkConnection;
import com.broll.networklib.server.impl.LobbySettings;
import com.broll.networklib.server.impl.ServerLobby;
import com.broll.networklib.site.NetworkSite;
import com.broll.networklib.site.SiteReceiver;
import com.esotericsoftware.minlog.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class NetworkTest<L extends LobbySettings, P extends LobbySettings> {

    private final static int TIMEOUT = 5000;
    private final static String IP = "localhost";
    protected TestServer server;
    protected LobbyGameServer<L, P> gameServer;
    protected Map<LobbyGameClient, TestClientData> clients = new HashMap<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();


    protected abstract IRegisterNetwork registerNetwork();

    @Before
    public void before() {
        Log.INFO();
        server = new TestServer(registerNetwork(), TIMEOUT);
        server.setSiteReceiver(tunneledReceiver());
        gameServer = new LobbyGameServer<>(server, "TestServer");
        registerServerSites(gameServer);
        gameServer.open();
    }

    private SiteReceiver tunneledReceiver() {
        return new SiteReceiver() {
            @Override
            public void receive(Object context, NetworkSite site, Method receiver, Object object) {
                executor.execute(() -> super.receive(context, site, receiver, object));
            }
        };
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
        testClient.setSiteReceiver(tunneledReceiver());
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
        ServerLobby<L, P> lobby = gameServer.getLobbyHandler().openLobby(name);
        lobby.setData(data);
        return lobby;
    }

    public void expectFailure(Runnable runnable, String failure) {
        try {
            runnable.run();
            Assert.fail("Expected failure: " + failure);
        } catch (RuntimeException e) {
            Assert.assertEquals("java.util.concurrent.ExecutionException: java.lang.RuntimeException: " + failure, e.getMessage());
        }
    }

    private void fail(AsyncTaskCallback task, String reason) {
        task.failed(new RuntimeException("Operation failed: " + reason));
    }

    public void joinLobby(LobbyGameClient client, ServerLobby serverLobby) {
        GameLobby lobby = AsyncTask.doAsync(task ->
                client.listLobbies(new ILobbyDiscovery() {
                    @Override
                    public void discovered(String serverIp, String serverName, List<GameLobby> lobbies) {
                        String name = clients.get(client).playerName;
                        for (GameLobby lobby : lobbies) {
                            if (lobby.getLobbyId() == serverLobby.getId()) {
                                Log.info("Client " + name + " discovered lobby " + lobby.getName());
                                task.done(lobby);
                                return;
                            }
                        }
                        fail(task, "lobby not found");
                    }

                    @Override
                    public void noLobbiesDiscovered() {
                        fail(task, "no lobbies discovered");
                    }

                    @Override
                    public void discoveryDone() {

                    }
                })
        );
        connectToLobby(client, lobby);
    }

    private void connectToLobby(LobbyGameClient client, GameLobby lobby) {
        AsyncTask.doAsync(task -> {
                    String name = clients.get(client).playerName;
                    client.connectToLobby(lobby, name, new INetworkRequestAttempt<GameLobby>() {
                        @Override
                        public void failure(String reason) {
                            fail(task, "unable to join lobby " + reason);
                        }

                        @Override
                        public void receive(GameLobby response) {
                            //joined lobby
                            Log.info("Client " + name + " joined lobby " + lobby.getName());
                            task.done(true);
                        }
                    });
                }
        );
    }


    private class TestClientData {
        public String playerName;
        public TestClient testClient;
    }
}
