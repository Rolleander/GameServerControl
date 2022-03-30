package com.broll.networklib.server.impl;

public interface ILobbyCreationRequest<L  extends ILobbyData, P extends ILobbyData> {

    ServerLobby<L, P> createNewLobby(Player<P> requester, String lobbyName, Object settings);
}
