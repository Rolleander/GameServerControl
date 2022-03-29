package com.broll.networklib.server.impl;

import java.util.List;

public interface LobbyCloseListener<L  extends LobbySettings ,P extends LobbySettings> {

    void kickedPlayer(Player<P> player);
    void closed(ServerLobby<L, P> lobby, List<Player<P>> players);
}
