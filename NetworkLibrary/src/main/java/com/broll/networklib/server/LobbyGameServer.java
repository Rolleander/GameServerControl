package com.broll.networklib.server;

import com.broll.networklib.server.impl.ConnectionSite;
import com.broll.networklib.server.impl.LobbyCloseListener;
import com.broll.networklib.server.impl.LobbyHandler;
import com.broll.networklib.server.impl.LobbySite;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ServerLobby;

import java.util.Arrays;
import java.util.List;

public class LobbyGameServer<L, P> {

    private GameServer server;
    private LobbyHandler<L,P> lobbyHandler;
    private ConnectionSite<L,P> connectionSite;

    public LobbyGameServer(String name) {
        server = new GameServer();
        lobbyHandler = new LobbyHandler<>(new LobbyCloseListener<L,P>() {
            @Override
            public void closed(ServerLobby<L,P> lobby, List<Player<P>> players) {
                connectionSite.closedLobby(lobby, players);
            }

            @Override
            public void kickedPlayer(Player<P> player) {
                connectionSite.kickedPlayer(player);
            }
        });
        server.setSitesHandler(new LobbyServerSitesHandler<>(lobbyHandler,type -> server.registerNetworkType(type)));
        connectionSite = new ConnectionSite(name);
        register(connectionSite);
        register(new LobbySite());
    }

    public void register(LobbyServerSite<L, P>... sites) {
        Arrays.stream(sites).forEach(site->site.init(lobbyHandler));
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
