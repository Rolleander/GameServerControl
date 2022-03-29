package com.broll.networklib.server;

import com.broll.networklib.server.impl.Player;
import com.esotericsoftware.kryonet.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkConnection extends Connection {

    private Player player;

    private boolean active = true;

    public void setPlayer(Player player) {
        this.player = player;
        setName("Connection "+getID()+" player ["+player+"]");
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
