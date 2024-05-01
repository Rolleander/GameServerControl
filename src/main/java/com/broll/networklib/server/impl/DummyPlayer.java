package com.broll.networklib.server.impl;

import com.broll.networklib.server.NetworkConnection;

public class DummyPlayer<P extends ILobbyData> extends Player<P>{
    public DummyPlayer(int id) {
        super(id, null, null);
    }

    @Override
    public void sendTCP(Object object) {

    }

    @Override
    public void sendUDP(Object object) {

    }

    @Override
    public boolean isOnline() {
        return false;
    }
}
