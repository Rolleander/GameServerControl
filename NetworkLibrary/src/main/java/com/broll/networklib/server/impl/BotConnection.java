package com.broll.networklib.server.impl;

import com.broll.networklib.client.ClientSite;
import com.broll.networklib.server.NetworkConnection;
import com.broll.networklib.site.SitesHandler;

import java.util.Arrays;

public class BotConnection<P extends LobbySettings> extends NetworkConnection {

    private SitesHandler<BotSite, Object> sites = new SitesHandler<>();
    private BotEndpoint endpoint;

    public BotConnection() {
    }

    public void init(BotEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void register(BotSite... sites) {
        Arrays.asList(sites).forEach(site -> {
            this.sites.add(site);
            site.init(endpoint);
        });
    }

    public void unregister(BotSite... sites) {
        Arrays.asList(sites).forEach(site -> this.sites.remove(site));
    }

    private int received(Object o) {
        sites.pass(null, o, sites -> {
        });
        return 0;
    }

    public BotPlayer<P> getBot() {
        return (BotPlayer<P>) getPlayer();
    }

    @Override
    public int sendTCP(Object o) {
        return received(o);
    }

    @Override
    public int sendUDP(Object o) {
        return received(o);
    }

}
