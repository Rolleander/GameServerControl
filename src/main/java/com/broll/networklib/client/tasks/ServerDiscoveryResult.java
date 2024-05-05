package com.broll.networklib.client.tasks;

import com.broll.networklib.client.impl.GameLobby;

import java.util.List;

public class ServerDiscoveryResult {

    private List<ServerInformation> servers;
    private GameLobby reconnected;

    public ServerDiscoveryResult(List<ServerInformation> servers){
        this.servers = servers;
    }

    public ServerDiscoveryResult(GameLobby reconnected){
        this.reconnected = reconnected;
    }

    public List<ServerInformation> getServers() {
        return servers;
    }

    public GameLobby getReconnectedLobby() {
        return reconnected;
    }
}
