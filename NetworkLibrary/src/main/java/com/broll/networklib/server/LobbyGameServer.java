package com.broll.networklib.server;

import com.broll.networklib.server.impl.ConnectionSite;
import com.broll.networklib.server.impl.LobbyHandler;
import com.broll.networklib.server.impl.LobbySite;

public class LobbyGameServer<L, P> {

    private GameServer server;
    private LobbyHandler<L,P> lobbyHandler;
    private ConnectionSite<L,P> connectionSite;

    public LobbyGameServer() {
        server = new GameServer();
        lobbyHandler = new LobbyHandler<>((lobby, players) -> connectionSite.closedLobby(lobby, players));
        connectionSite = new ConnectionSite(lobbyHandler);
        register(connectionSite);
        register(new LobbySite(lobbyHandler));
    }

    public void register(LobbyServerSite<L, P>... sites) {
        server.register(sites);
    }

    public void open() {
        server.open();
    }

    public void shutdown() {
        lobbyHandler.closeAllLobbies();
        server.shutdown();
    }
}
