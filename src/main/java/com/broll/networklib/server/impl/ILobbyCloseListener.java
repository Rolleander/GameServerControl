package com.broll.networklib.server.impl;

import java.util.List;

public interface ILobbyCloseListener<L  extends ILobbyData,P extends ILobbyData> {

    void kickedPlayer(Player<P> player);
    void closed(ServerLobby<L, P> lobby, List<Player<P>> players);
}
