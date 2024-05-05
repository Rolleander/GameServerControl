package com.broll.networklib.client.tasks;

import com.broll.networklib.client.impl.GameLobby;

public class ServerResult {

    private ServerInformation server;
    private GameLobby reconnected;


    public ServerResult(ServerInformation server){
        this.server = server;
    }

    public ServerResult(GameLobby reconnected){
        this.reconnected = reconnected;
    }

    public ServerInformation getServer() {
        return server;
    }

    public GameLobby getReconnectedLobby() {
        return reconnected;
    }
}
