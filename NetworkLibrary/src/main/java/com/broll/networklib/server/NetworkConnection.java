package com.broll.networklib.server;

import com.broll.networklib.server.impl.Player;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;

public class NetworkConnection extends Connection {

    private Player player;

    private boolean active = true;

    public void setPlayer(Player player) {
        this.player = player;
        setName("Connection "+getID()+" player ["+player.getName()+"]");
    }

    public Player getPlayer() {
        return player;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
