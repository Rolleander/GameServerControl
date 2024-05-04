package com.broll.networklib.client.tasks;

import com.broll.networklib.client.impl.GameLobby;

public class LobbyListResult {

    private DiscoveredLobbies lobbies;
    private GameLobby reconnected;


    public LobbyListResult(DiscoveredLobbies lobbies){
        this.lobbies = lobbies;
    }

    public LobbyListResult(GameLobby reconnected){
        this.reconnected = reconnected;
    }

    public DiscoveredLobbies getLobbies() {
        return lobbies;
    }

    public GameLobby getReconnectedLobby() {
        return reconnected;
    }
}
