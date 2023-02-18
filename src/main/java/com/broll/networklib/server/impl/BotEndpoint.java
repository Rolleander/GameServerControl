package com.broll.networklib.server.impl;

import com.broll.networklib.GameEndpoint;
import com.broll.networklib.server.LobbyServerSitesHandler;
import com.broll.networklib.server.ServerSite;
import com.esotericsoftware.kryo.Kryo;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class BotEndpoint<L extends ILobbyData, P extends ILobbyData> extends GameEndpoint<BotSite, Object> {

    private final static int BOT_DELAY = 100;
    private LobbyServerSitesHandler<L, P> sitesHandler;
    private BotConnection<P> botConnection;
    private ScheduledExecutorService executor;

    public BotEndpoint(LobbyServerSitesHandler sitesHandler, BotConnection botConnection) {
        super(null, sitesHandler);
        this.sitesHandler = sitesHandler;
        this.botConnection = botConnection;
        int id = botConnection.getBot().getId();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("bot-"+id).build();
        this.executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        sitesHandler.initConnection(botConnection);
    }

    void schedule(Runnable runnable) {
        if (!executor.isShutdown()) {
            executor.schedule(runnable, BOT_DELAY, TimeUnit.MILLISECONDS);
        }
    }

    public void send(Object object) {
        schedule(() -> sitesHandler.pass(botConnection, object, sites -> initSites(sites, object)));
    }

    private void initSites(Collection<ServerSite> sites, Object object) {
        sites.forEach(site -> site.receive(botConnection, object));
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
