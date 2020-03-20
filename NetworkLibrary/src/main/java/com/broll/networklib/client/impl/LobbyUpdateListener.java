package com.broll.networklib.client.impl;

public interface LobbyUpdateListener {

    void lobbyUpdated();

    void playerJoined(LobbyPlayer player);

    void playerLeft(LobbyPlayer player);

    void kickedFromLobby();

    void closed();
}
