package com.broll.networklib.client.tasks;

import com.broll.networklib.client.impl.GameLobby;

import java.util.List;

public class LobbyDiscoveryResult {

    private List<DiscoveredLobbies> lobbies;
    private GameLobby reconnected;

    public LobbyDiscoveryResult(List<DiscoveredLobbies> lobbies){
        this.lobbies = lobbies;
    }

    public LobbyDiscoveryResult(GameLobby reconnected){
        this.reconnected = reconnected;
    }

    public List<DiscoveredLobbies> getLobbies() {
        return lobbies;
    }

    public GameLobby getReconnectedLobby() {
        return reconnected;
    }
}
