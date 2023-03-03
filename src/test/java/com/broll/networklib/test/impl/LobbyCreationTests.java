package com.broll.networklib.test.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.broll.networklib.client.LobbyGameClient;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.LobbyGameServer;
import com.broll.networklib.server.impl.ILobbyCreationRequest;
import com.broll.networklib.server.impl.ILobbyData;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ServerLobby;
import com.broll.networklib.test.NetworkTest;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.ExecutionException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LobbyCreationTests extends NetworkTest {

    @Override
    protected IRegisterNetwork registerNetwork() {
        return (register) -> {
            register.registerNetworkType(Settings.class);
        };
    }

    @Override
    public void registerServerSites(LobbyGameServer server) {

    }

    @Override
    public void registerClientSites(LobbyGameClient client) {

    }

    @Override
    public void before() {
        super.before();
        gameServer.getLobbyHandler().setLobbyCreationRequestHandler(
                (player, lobbyName, settings) -> gameServer.getLobbyHandler().openLobby(lobbyName));
    }

    @Test
    public void versionMismatch() {
        LobbyGameClient client = testClient("Tester");
        client.setVersion("different");
        //client should not be able to join
        expectFailure(() ->  createLobby(client, null), "java.util.concurrent.ExecutionException: com.broll.networklib.network.NetworkException: java.util.concurrent.ExecutionException: java.lang.Exception: Could not create lobby: Version mismatch with server: null");
    }

    @Test
    public void createLobby() {
        LobbyGameClient client = testClient("Tester");
        GameLobby lobby = createLobby(client, null);
        assertEquals("Tester's Lobby", lobby.getName());
        assertEquals(lobby.getMyPlayer(), lobby.getOwner());
        ServerLobby serverLobby = gameServer.getLobbyHandler().getLobby(lobby.getLobbyId());
        assertEquals(serverLobby.getPlayerCount(), lobby.getPlayerCount());
    }

    @Test
    public void createLobbyWithSettings() {
        LobbyGameClient client = testClient("Tester");
        Settings lobbySettings = new Settings();
        lobbySettings.maxPlayers = 5;
        lobbySettings.name = "coolLobby";
        gameServer.getLobbyHandler().setLobbyCreationRequestHandler((requester, lobbyName, settings) -> {
            ServerLobby lobby = gameServer.getLobbyHandler().openLobby("test");
            Settings st = (Settings) settings;
            lobby.setPlayerLimit(st.maxPlayers);
            lobby.setName(st.name);
            return lobby;
        });
        GameLobby lobby = createLobby(client, lobbySettings);
        assertEquals(lobbySettings.name, lobby.getName());
        assertEquals(lobbySettings.maxPlayers, lobby.getPlayerLimit());
        ServerLobby serverLobby = gameServer.getLobbyHandler().getLobby(lobby.getLobbyId());
        assertEquals(lobbySettings.name, serverLobby.getName());
        assertEquals(lobbySettings.maxPlayers, serverLobby.getPlayerLimit());
    }

    private static class Settings{
        int maxPlayers;
        String name;
    }
}
