package com.broll.networklib.client.impl;

public interface ILobbyUpdateListener {

    void lobbyUpdated(GameLobby lobby);

    void playerJoined(GameLobby lobby, LobbyPlayer player);

    void playerLeft(GameLobby lobby, LobbyPlayer player);

    void kickedFromLobby(GameLobby lobby);

    void closed(GameLobby lobby);

    void disconnected(GameLobby lobby);
}
