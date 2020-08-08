package com.broll.networklib.server.impl;

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

}
