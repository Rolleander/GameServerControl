package com.broll.networklib.client;

import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.LobbyPlayer;

public abstract class LobbyClientSite extends ClientSite {

    protected LobbyGameClient lobbyGameClient;

    void init(LobbyGameClient lobbyGameClient) {
        this.lobbyGameClient = lobbyGameClient;
    }

    protected GameLobby getLobby() {
        return lobbyGameClient.getConnectedLobby();
    }

    protected LobbyPlayer getPlayer() {
        return getLobby().getMyPlayer();
    }
}
