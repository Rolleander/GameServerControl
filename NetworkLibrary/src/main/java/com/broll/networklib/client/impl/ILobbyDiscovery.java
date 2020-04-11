package com.broll.networklib.client.impl;

import com.broll.networklib.client.impl.GameLobby;

import java.util.List;

public interface ILobbyDiscovery {

    void discovered(String serverIp, String serverName, List<GameLobby> lobbies);

    void noLobbiesDiscovered();

    void discoveryDone();
}
