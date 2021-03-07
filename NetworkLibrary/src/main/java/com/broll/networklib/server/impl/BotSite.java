package com.broll.networklib.server.impl;

import com.broll.networklib.client.ClientSite;
import com.broll.networklib.site.NetworkSite;

public class BotSite<P extends LobbySettings> extends NetworkSite<BotEndpoint> {

    private BotEndpoint botEndpoint;
    private BotPlayer<P> bot;

    @Override
    public void init(BotEndpoint endpoint) {
        super.init(endpoint);
        this.botEndpoint = endpoint;
        this.bot= botEndpoint.getBot();
    }

    public void sendServer(Object object) {
        botEndpoint.send(object);
    }

    public BotPlayer<P> getBot() {
        return bot;
    }
}
