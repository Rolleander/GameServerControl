package com.broll.networklib.server.impl;


public class DummyServerLobby<L extends ILobbyData, P extends ILobbyData> extends ServerLobby<L,P>{
    public DummyServerLobby() {
        super(null, null, -1, null);
    }

    public void addDummyPlayer(DummyPlayer<P> player){
        addPlayer(player);
    }

}
