package com.broll.networklib.server.impl;

public class DummyLobbyPlayer<P extends ILobbyData> extends LobbyPlayer<P>{
    public DummyLobbyPlayer(int id) {
        super(new DummyPlayer<>(id));
    }

}
