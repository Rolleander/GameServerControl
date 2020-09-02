package com.broll.networklib.client.impl;

public interface ILobbyConnectionListener {

    void lobbyJoined(GameLobby lobby);

    void leftLobby();
}
