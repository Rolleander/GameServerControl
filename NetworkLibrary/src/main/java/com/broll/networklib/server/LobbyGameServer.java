package com.broll.networklib.server;

import com.broll.networklib.NetworkRegister;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.server.impl.ConnectionSite;
import com.broll.networklib.server.impl.LobbyCloseListener;
import com.broll.networklib.server.impl.LobbyHandler;
import com.broll.networklib.server.impl.LobbySite;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.LobbySettings;
import com.broll.networklib.server.impl.PlayerRegister;
import com.broll.networklib.server.impl.ServerLobby;
import com.esotericsoftware.kryo.Kryo;

import java.util.Arrays;
import java.util.List;

public class LobbyGameServer<L extends LobbySettings, P extends LobbySettings> implements NetworkRegister {

    private GameServer server;
    private LobbyHandler<L, P> lobbyHandler;
    private ConnectionSite<L, P> connectionSite;

    public LobbyGameServer(String name, IRegisterNetwork registerNetwork) {
        this(new GameServer(registerNetwork), name);
    }

    public LobbyGameServer(GameServer server, String name) {
        this.server = server;
        PlayerRegister playerRegister = new PlayerRegister();
        connectionSite = new ConnectionSite(name, playerRegister);
        lobbyHandler = new LobbyHandler<>(new LobbyCloseListener<L, P>() {
            @Override
            public void closed(ServerLobby<L, P> lobby, List<Player<P>> players) {
                connectionSite.closedLobby(lobby, players);
            }

            @Override
            public void kickedPlayer(Player<P> player) {
                connectionSite.kickedPlayer(player);
            }
        }, playerRegister);
        server.setSitesHandler(lobbyHandler.getSitesHandler());
        register(connectionSite);
        register(new LobbySite());
    }

    @Override
    public Kryo getKryo() {
        return server.getKryo();
    }

    public LobbyServerCLI initCLI() {
        return new LobbyServerCLI(this);
    }

    public LobbyHandler<L, P> getLobbyHandler() {
        return lobbyHandler;
    }

    @Override
    public void registerNetworkPackage(String packagePath) {
        server.registerNetworkPackage(packagePath);
    }

    @Override
    public void registerNetworkType(Class type) {
        server.registerNetworkType(type);
    }

    public void register(LobbyServerSite<L, P>... sites) {
        Arrays.stream(sites).forEach(site -> site.init(lobbyHandler));
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
