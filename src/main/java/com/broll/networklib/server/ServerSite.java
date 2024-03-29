package com.broll.networklib.server;

import com.broll.networklib.site.NetworkSite;

public abstract class ServerSite extends NetworkSite<GameServer> {

    protected GameServer server;
    private NetworkConnection connection;

    public void init(GameServer server) {
        super.init(server);
        this.server = server;
    }

    public GameServer getServer() {
        return server;
    }

    public NetworkConnection getConnection() {
        return connection;
    }

    public void receive(NetworkConnection connection, Object object) {
        this.connection = connection;
    }

    public <T extends ServerSite> T accessSite(Class<T> siteClass) {
        T site = (T) server.getSiteInstances(connection).get(siteClass);
        site.receive(connection, null);
        return site;
    }

    public void onConnect(NetworkConnection connection) {
        this.connection = connection;
    }

    public void onDisconnect(NetworkConnection connection) {

    }
}
