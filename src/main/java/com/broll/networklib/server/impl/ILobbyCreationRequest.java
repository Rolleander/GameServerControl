package com.broll.networklib.server.impl;

public interface ILobbyCreationRequest<L  extends LobbySettings, P extends LobbySettings> {

    ServerLobby<L, P> createNewLobby(Player<P> requester, String lobbyName, Object settings);
}
