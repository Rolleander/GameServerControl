package com.broll.networklib.server.impl;

public interface ServerLobbyListener<L  extends LobbySettings,P  extends LobbySettings> {

    void playerJoined(ServerLobby<L,P> lobby,Player<P>  player);

    void playerLeft(ServerLobby<L,P> lobby, Player<P> player);

    void lobbyClosed(ServerLobby<L,P> lobby);

}
