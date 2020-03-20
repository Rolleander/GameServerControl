package com.broll.networklib.server.impl;

public interface ILobbyCreationRequest<L, P> {

    ServerLobby<L, P> createNewLobby(Player<P> requester, String lobbyName, Object settings);
}
