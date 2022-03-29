package com.broll.networklib.client.impl;

public interface ChatMessageListener {
    void fromPlayer(String msg, LobbyPlayer from);

    void fromGame(String msg);
}
