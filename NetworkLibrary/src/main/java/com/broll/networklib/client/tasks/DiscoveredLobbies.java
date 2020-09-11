package com.broll.networklib.client.tasks;

import com.broll.networklib.client.impl.GameLobby;

import java.util.List;

public class DiscoveredLobbies {

    private String serverName;
    private String serverIp;
    private List<GameLobby> lobbies;

    public DiscoveredLobbies(String serverName, String serverIp, List<GameLobby> lobbies) {
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.lobbies = lobbies;
    }

    public List<GameLobby> getLobbies() {
        return lobbies;
    }

    public String getServerIp() {
        return serverIp;
    }

    public String getServerName() {
        return serverName;
    }
}
