package com.broll.networklib.client.impl;

public interface IChatMessageListener {
    void fromPlayer(String msg, LobbyPlayer from);

    void fromGame(String msg);
}
