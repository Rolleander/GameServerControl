package com.broll.networklib.server.impl;

import java.util.List;

public interface LobbyCloseListener<L ,P> {

    void closed(ServerLobby<L, P> lobby, List<Player<P>> players);
}
