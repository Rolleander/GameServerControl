package com.broll.networklib.test.impl;

import com.broll.networklib.client.LobbyGameClient;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.LobbyGameServer;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ServerLobby;
import com.broll.networklib.test.NetworkTest;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LobbyConnectionTests extends NetworkTest {

    @Override
    protected IRegisterNetwork registerNetwork() {
        return (register) -> {
        };
    }

    @Override
    public void registerServerSites(LobbyGameServer server) {

    }

    @Override
    public void registerClientSites(LobbyGameClient client) {

    }

    @Test
    public void connect() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient peter = testClient("Peter", lobby);
        LobbyGameClient pan = testClient("Pan", lobby);
        sleep();
        assertEquals(2, lobby.getActivePlayers().size());
        dropPackages();
    }

    @Test
    public void disconnect() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient peter = testClient("Peter", lobby);
        LobbyGameClient pan = testClient("Pan", lobby);
        sleep();
        assertEquals(2, lobby.getActivePlayers().size());
        pan.shutdown();
        sleep();
        assertEquals(1, lobby.getActivePlayers().size());
        dropPackages();
    }

    @Test
    public void lobbyTransfer() {
        ServerLobby lobby1 = openGameLobby(null, "TestLobby");
        ServerLobby lobby2 = openGameLobby(null, "TestLobby2");
        assertEquals(2, gameServer.getLobbyHandler().getLobbies().size());
        LobbyGameClient peter = testClient("Peter", lobby1);
        assertEquals(1, lobby1.getActivePlayers().size());
        assertEquals(0, lobby2.getActivePlayers().size());
        Player serverPeter = (Player) lobby1.getActivePlayers().iterator().next();
        joinLobby(peter, lobby2);
        sleep();
        assertEquals(0, lobby1.getActivePlayers().size());
        assertTrue(lobby1.isClosed());
        assertEquals(1, gameServer.getLobbyHandler().getLobbies().size());
        assertEquals(1, lobby2.getActivePlayers().size());
        assertEquals(serverPeter, lobby2.getActivePlayers().iterator().next());
        dropPackages();
    }

    @Test
    public void doubleJoin() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient peter = testClient("Peter", lobby);
        sleep();
        assertEquals(1, lobby.getActivePlayers().size());
        joinLobby(peter, lobby);
        sleep();
        assertEquals(1, lobby.getActivePlayers().size());
        dropPackages();
    }

    @Test
    public void kickPlayer() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient peter = testClient("Peter", lobby);
        LobbyGameClient hans = testClient("Hans", lobby);
        sleep();
        assertEquals(2, lobby.getActivePlayers().size());
        lobby.kickPlayer(lobby.getPlayer(0));
        assertEquals(1, lobby.getActivePlayers().size());
        dropPackages();
    }

    @Test
    public void lockLobby() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient peter = testClient("Peter", lobby);
        lobby.lock();
        //client should not find locked lobby
        expectFailure(() -> testClient("Pan", lobby), "Lobby not found");
        assertEquals(1, lobby.getActivePlayers().size());
        //unlock and try again joining
        lobby.unlock();
        testClient("Pan", lobby);
        assertEquals(2, lobby.getActivePlayers().size());
        dropPackages();
    }

    @Test
    public void lobbyFull() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        lobby.setPlayerLimit(1);
        LobbyGameClient peter = testClient("Peter", lobby);
        sleep();
        //client should not be able to join
        expectFailure(() -> testClient("Pan", lobby), "java.util.concurrent.ExecutionException: com.broll.networklib.network.NetworkException: java.util.concurrent.ExecutionException: java.lang.Exception: Could not join lobby: null");
        assertEquals(1, lobby.getActivePlayers().size());
        dropPackages();
    }

    @Test
    public void versionMismatch() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient client = testClient("Tester");
        client.setVersion("different");
        //client should not be able to join
        expectFailure(() ->  joinLobby(client, lobby), "java.util.concurrent.ExecutionException: com.broll.networklib.network.NetworkException: java.util.concurrent.ExecutionException: java.lang.Exception: Could not list lobbies: Version mismatch with server: null");
    }
    @Test
    public void reconnect() throws ExecutionException, InterruptedException {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient peter = testClient("Peter", lobby);
        lobby.lock();
        peter.shutdown();
        sleep();
        assertEquals(1, lobby.getActivePlayers().size());
        assertEquals(false, lobby.getPlayer(0).isOnline());
        //another player should not be able to reconnect
        expectFailure(() -> waitFor(testClient("Hans").reconnectCheck("localhost")),
                "java.util.concurrent.ExecutionException: com.broll.networklib.network.NetworkException: java.util.concurrent.ExecutionException: java.lang.Exception: Could not reconnect player");
        //reconnect correct player
        sleep();
        peter = testClient("Peter");
        sleep();
        GameLobby gameLobby = (GameLobby) waitFor(peter.reconnectCheck("localhost"));
        assertEquals(lobby.getId(), gameLobby.getLobbyId());
        assertEquals(1, lobby.getActivePlayers().size());
        assertEquals(true, lobby.getPlayer(0).isOnline());
        dropPackages();
    }

}
