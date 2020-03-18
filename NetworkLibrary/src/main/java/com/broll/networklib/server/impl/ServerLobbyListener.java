package com.broll.networklib.server.impl;

public interface ServerLobbyListener {

    void playerJoined(ServerLobby lobby,Player player);

    void playerLeft(ServerLobby lobby, Player player);

}
