package com.broll.networklib.server;

import com.broll.networklib.player.Player;
import com.esotericsoftware.kryonet.Connection;

public class NetworkConnection extends Connection {

    private Player player;

    private boolean active = true;

    public void setPlayer(Player player){
        this.player = player;
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
