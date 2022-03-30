package com.broll.networklib.server.impl;

import com.broll.networklib.server.NetworkConnection;
import com.broll.networklib.site.SingleSitesHandler;

import java.util.Arrays;

public class BotConnection<P extends ILobbyData> extends NetworkConnection {

    private SingleSitesHandler<BotSite, Object> sites = new SingleSitesHandler<>();
    private BotEndpoint endpoint;

    public BotConnection() {
        sites.setUnknownMessageReceiver(this::receivedUnknownMessage);
    }

    private void receivedUnknownMessage(Object message) {
        //bots should not show errors for unhandled network messages
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
        endpoint.schedule(() -> sites.pass(null, o, site -> {
        }));
        return 0;
    }

    @Override
    public void close() {
        endpoint.shutdown();
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
