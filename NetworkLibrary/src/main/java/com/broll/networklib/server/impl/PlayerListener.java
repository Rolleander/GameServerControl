package com.broll.networklib.server.impl;

public interface PlayerListener<L  extends LobbySettings,P extends LobbySettings> {
    void joinedLobby(Player<P> player, ServerLobby<L,P> serverLobby);

    void leftLobby(Player<P> player, ServerLobby<L,P> serverLobby);

    void switchedLobby(Player<P> player, ServerLobby<L,P> from, ServerLobby<L,P> to);

    void disconnected(Player<P> player);

    void reconnected(Player<P> player);
}
