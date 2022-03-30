package com.broll.networklib.server.impl;

public interface IServerLobbyListener<L  extends ILobbyData,P  extends ILobbyData> {

    void playerJoined(ServerLobby<L,P> lobby,Player<P>  player);

    void playerLeft(ServerLobby<L,P> lobby, Player<P> player);

    void playerDisconnected(ServerLobby<L,P> lobby, Player<P> player);

    void playerReconnected(ServerLobby<L,P> lobby, Player<P> player);

    void lobbyClosed(ServerLobby<L,P> lobby);

}
