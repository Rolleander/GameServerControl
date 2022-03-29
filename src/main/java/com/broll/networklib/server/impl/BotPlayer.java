package com.broll.networklib.server.impl;

import com.broll.networklib.network.nt.NT_LobbyPlayerInfo;
import com.esotericsoftware.minlog.Log;

public class BotPlayer<P extends LobbySettings> extends Player<P> {

    private final static String KEY_PREFIX = "BOT_";

    BotPlayer(int id, BotConnection botConnection) {
        super(id, KEY_PREFIX + id, botConnection);
    }

    private BotConnection getBotConnection() {
        return (BotConnection) this.getConnection();
    }

    public void register(BotSite<P>... sites) {
        getBotConnection().register(sites);
    }

    public void unregister(BotSite<P>... sites) {
        getBotConnection().unregister(sites);
    }

    @Override
    void removedFromLobby() {
        super.removedFromLobby();
        remove();
    }

    public boolean remove() {
        ServerLobby lobby = getServerLobby();
        if (lobby != null) {
            if (lobby.isLocked()) {
                Log.error("Cannot remove bot since it is part of a locked lobby!");
                return false;
            }
            lobby.removePlayer(this);
        }
        getBotConnection().close();
        return true;
    }

    @Override
    NT_LobbyPlayerInfo nt() {
        NT_LobbyPlayerInfo info = super.nt();
        info.bot = true;
        return info;
    }
}
