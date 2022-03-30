package com.broll.networklib.examples.lobby.server;

import com.broll.networklib.examples.lobby.nt.NT_LobbySettings;
import com.broll.networklib.server.impl.ILobbyData;

public class MonopolyLobbyData implements ILobbyData {

    private int startMoney = 500;

    public void setStartMoney(int startMoney) {
        this.startMoney = startMoney;
    }

    @Override
    public Object nt() {
        NT_LobbySettings settings = new NT_LobbySettings();
        settings.startMoney = startMoney;
        return settings;
    }

}
