package com.broll.networklib.server;

import com.broll.networklib.NetworkRegister;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.impl.ConnectionSite;
import com.broll.networklib.server.impl.ILobbyCloseListener;
import com.broll.networklib.server.impl.LobbyHandler;
import com.broll.networklib.server.impl.LobbySite;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ILobbyData;
import com.broll.networklib.server.impl.PlayerRegister;
import com.broll.networklib.server.impl.ServerLobby;
import com.broll.networklib.site.SiteReceiver;
import com.esotericsoftware.kryo.Kryo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LobbyGameServer<L extends ILobbyData, P extends ILobbyData> implements NetworkRegister {

    private GameServer server;
    private LobbyHandler<L, P> lobbyHandler;
    private ConnectionSite<L, P> connectionSite;
    private Map<String, Object> sharedData = new HashMap<>();

    private LobbyServerListenerProxy<L,P> listeners = new LobbyServerListenerProxy<>();

    public LobbyGameServer(String name, IRegisterNetwork registerNetwork) {
        this(new GameServer(registerNetwork), name);
    }

    public LobbyGameServer(String name, LobbySite<L,P> lobbySite, IRegisterNetwork registerNetwork) {
        this(new GameServer(registerNetwork), name, lobbySite);
    }

    public LobbyGameServer(GameServer server, String name) {
        this(server,name,new LobbySite<>());
    }

    public LobbyGameServer(GameServer server, String name, LobbySite<L,P> lobbySite) {
        this.server = server;
        PlayerRegister playerRegister = new PlayerRegister();
        connectionSite = new ConnectionSite(name, playerRegister);
        LobbyServerSitesHandler<L, P> sitesHandler = new LobbyServerSitesHandler<>();
        lobbyHandler = new LobbyHandler<>(new ILobbyCloseListener<L, P>() {
            @Override
            public void closed(ServerLobby<L, P> lobby, List<Player<P>> players) {
                connectionSite.closedLobby(lobby, players);
            }

            @Override
            public void kickedPlayer(Player<P> player) {
                connectionSite.kickedPlayer(player);
            }
        }, playerRegister, sitesHandler, listeners);
        server.setSitesHandler(sitesHandler);
        register(connectionSite);
        register(lobbySite);
    }

    public void addListener(ILobbyServerListener<L, P> listener) {
        this.listeners.addListener(listener);
    }

    public void removeListener(ILobbyServerListener<L, P> listener) {
        this.listeners.removeListener(listener);
    }

    public void setVersion(String version) {
        this.connectionSite.setVersion(version);
    }
    public void setSiteReceiver(SiteReceiver<ServerSite, NetworkConnection> receiver) {
        server.setSiteReceiver(receiver);
    }

    @Override
    public Kryo getKryo() {
        return server.getKryo();
    }

    LobbyServerCLI initCLI() {
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
        Arrays.stream(sites).forEach(site -> site.init(this, lobbyHandler));
        server.register(sites);
    }

    public void unregister(LobbyServerSite<L, P>... sites) {
        server.unregister(sites);
    }

    public void clearSites() {
        server.clearSites();
    }

    public void open() {
        server.open();
    }

    public void open(int threadPoolSize) {
        server.open(threadPoolSize);
    }

    public void shutdown() {
        lobbyHandler.closeAllLobbies();
        server.shutdown();
    }

    Map<String, Object> getSharedData() {
        return sharedData;
    }
}
