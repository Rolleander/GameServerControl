package com.broll.networklib.server.impl;

import com.broll.networklib.GameEndpoint;
import com.broll.networklib.client.ClientSite;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.LobbyServerSitesHandler;
import com.esotericsoftware.kryo.Kryo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotEndpoint<L extends LobbySettings, P extends LobbySettings> extends GameEndpoint<BotSite, Object> {

    private final static int BOT_DELAY = 100;
    private LobbyServerSitesHandler<L, P> sitesHandler;
    private BotConnection<P> botConnection;
    private ScheduledExecutorService executor;

    public BotEndpoint(LobbyServerSitesHandler sitesHandler, BotConnection botConnection) {
        super(null, sitesHandler);
        this.sitesHandler = sitesHandler;
        this.botConnection = botConnection;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        sitesHandler.initConnection(botConnection);
    }

    void schedule(Runnable runnable) {
        executor.schedule(runnable, BOT_DELAY, TimeUnit.MILLISECONDS);
    }

    public void send(Object object) {
        schedule(() -> sitesHandler.pass(botConnection, object, sites -> sites.forEach(site -> site.receive(botConnection, object))));
    }

    public BotPlayer<P> getBot() {
        return botConnection.getBot();
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public Kryo getKryo() {
        return null;
    }
}
