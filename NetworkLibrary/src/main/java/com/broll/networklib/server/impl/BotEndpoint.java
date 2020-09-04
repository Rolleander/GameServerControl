package com.broll.networklib.server.impl;

import com.broll.networklib.GameEndpoint;
import com.broll.networklib.client.ClientSite;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.LobbyServerSitesHandler;
import com.esotericsoftware.kryo.Kryo;

public class BotEndpoint<L extends LobbySettings, P extends LobbySettings> extends GameEndpoint<BotSite, Object> {

    private LobbyServerSitesHandler<L, P> sitesHandler;
    private BotConnection<P> botConnection;

    public BotEndpoint(LobbyServerSitesHandler sitesHandler, BotConnection botConnection) {
        super(null, sitesHandler);
        this.sitesHandler = sitesHandler;
        this.botConnection = botConnection;
    }

    public void send(Object object) {
        sitesHandler.pass(botConnection, object, sites -> sites.forEach(site -> site.receive(botConnection, object)));
    }

    public BotPlayer<P> getBot() {
        return botConnection.getBot();
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Kryo getKryo() {
        return null;
    }
}
