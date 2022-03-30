package com.broll.networklib.examples.lobby.client;

import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.ILobbyUpdateListener;
import com.broll.networklib.client.impl.LobbyPlayer;

public class LobbyUpdateListener implements ILobbyUpdateListener {
    @Override
    public void lobbyUpdated(GameLobby lobby) {

    }

    @Override
    public void playerJoined(GameLobby lobby, LobbyPlayer player) {

    }

    @Override
    public void playerLeft(GameLobby lobby, LobbyPlayer player) {

    }

    @Override
    public void kickedFromLobby(GameLobby lobby) {

    }

    @Override
    public void closed(GameLobby lobby) {

    }

    @Override
    public void disconnected(GameLobby lobby) {

    }
}
