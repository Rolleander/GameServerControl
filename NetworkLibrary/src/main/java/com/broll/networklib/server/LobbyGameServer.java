package com.broll.networklib.server;

import com.broll.networklib.server.GameServer;
import com.broll.networklib.server.ServerSite;
import com.broll.networklib.server.impl.ConnectionSite;
import com.broll.networklib.server.impl.LobbyHandler;
import com.broll.networklib.server.impl.LobbySite;

public class LobbyGameServer {

    private GameServer server;
    private LobbyHandler lobbyHandler;

    public LobbyGameServer(){
        server = new GameServer();
        lobbyHandler =new LobbyHandler();
        register(new ConnectionSite(lobbyHandler));
        register(new LobbySite(lobbyHandler));
    }

    public LobbyHandler getLobbyHandler() {
        return lobbyHandler;
    }

    public void register(ServerSite... sites){
        server.register(sites);
    }

    public void open(){
        server.open();
    }

    public void shutdown(){
        server.shutdown();
    }
}
