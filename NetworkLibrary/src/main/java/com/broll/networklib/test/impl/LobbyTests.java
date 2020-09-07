package com.broll.networklib.test.impl;

import com.broll.networklib.client.LobbyGameClient;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.LobbyGameServer;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ServerLobby;
import com.broll.networklib.test.NetworkTest;
import com.esotericsoftware.minlog.Log;
import com.sun.media.jfxmedia.logging.Logger;

import org.junit.Test;
import org.testng.annotations.AfterTest;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LobbyTests extends NetworkTest {

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

        assertEquals(2, lobby.getPlayers().size());
        dropPackages();
    }

    @Test
    public void disconnect() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient peter = testClient("Peter", lobby);
        LobbyGameClient pan = testClient("Pan", lobby);
        assertEquals(2, lobby.getPlayers().size());
        server.disconnect(getTestClient(pan));
        assertEquals(1, lobby.getPlayers().size());
        dropPackages();
    }

    @Test
    public void lobbyTransfer() {
        ServerLobby lobby1 = openGameLobby(null, "TestLobby");
        ServerLobby lobby2 = openGameLobby(null, "TestLobby2");
        assertEquals(2, gameServer.getLobbyHandler().getLobbies().size());
        LobbyGameClient peter = testClient("Peter", lobby1);
        assertEquals(1, lobby1.getPlayers().size());
        assertEquals(0, lobby2.getPlayers().size());
        Player serverPeter = (Player) lobby1.getPlayers().iterator().next();
        joinLobby(peter, lobby2);
        assertEquals(0, lobby1.getPlayers().size());
        assertTrue(lobby1.isClosed());
        assertEquals(1, gameServer.getLobbyHandler().getLobbies().size());
        assertEquals(1, lobby2.getPlayers().size());
        assertEquals(serverPeter, lobby2.getPlayers().iterator().next());
        dropPackages();
    }

    @Test
    public void doubleJoin() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient peter = testClient("Peter", lobby);
        assertEquals(1, lobby.getPlayers().size());
        joinLobby(peter, lobby);
        assertEquals(1, lobby.getPlayers().size());
        dropPackages();
    }

    @Test
    public void kickPlayer() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient peter = testClient("Peter", lobby);
        LobbyGameClient hans = testClient("Hans", lobby);
        assertEquals(2, lobby.getPlayers().size());
        lobby.kickPlayer(lobby.getPlayer(0));
        assertEquals(1, lobby.getPlayers().size());
        dropPackages();
    }

    @Test
    public void lockLobby() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient peter = testClient("Peter", lobby);
        lobby.setLocked(true);
        //client should not find locked lobby
        expectFailure(() -> testClient("Pan", lobby), "Operation failed: lobby not found");
        assertEquals(1, lobby.getPlayers().size());
        //unlock and try again joining
        lobby.setLocked(false);
        testClient("Pan", lobby);
        assertEquals(2, lobby.getPlayers().size());
        dropPackages();
    }

    @Test
    public void lobbyFull() {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        lobby.setPlayerLimit(1);
        LobbyGameClient peter = testClient("Peter", lobby);
        //client should not be able to join
        expectFailure(() -> testClient("Pan", lobby), "Operation failed: unable to join lobby");
        assertEquals(1, lobby.getPlayers().size());
        dropPackages();
    }

    @Test
    public void reconnect() throws ExecutionException, InterruptedException {
        ServerLobby lobby = openGameLobby(null, "TestLobby");
        LobbyGameClient peter = testClient("Peter", lobby);
        lobby.setLocked(true);
        server.disconnect(getTestClient(peter));
        assertEquals(1, lobby.getPlayers().size());
        assertEquals(false, lobby.getPlayer(0).isOnline());
        //another player should not be able to reconnect
        assertFalse(testClient("Hans").checkForReconnection(null, reconLobby -> {
                    assertEquals(lobby.getId(), reconLobby.getLobbyId());
                }
        ).get().booleanValue());
        //reconnect correct player
        peter = testClient("Peter");
        assertTrue(peter.checkForReconnection(null, reconLobby -> {
                    assertEquals(lobby.getId(), reconLobby.getLobbyId());
                }
        ).get().booleanValue());
        assertEquals(1, lobby.getPlayers().size());
        assertEquals(true, lobby.getPlayer(0).isOnline());
        dropPackages();
    }


}
