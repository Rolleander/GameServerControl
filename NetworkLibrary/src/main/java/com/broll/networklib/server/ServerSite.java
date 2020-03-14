package com.broll.networklib.server;

import com.broll.networklib.site.NetworkSite;

public abstract class ServerSite extends NetworkSite<GameServer> {

    protected GameServer server;
    private NetworkConnection connection;

    public void init(GameServer server){
        super.init(server);
        this.server = server;
    }

    public GameServer getServer() {
        return server;
    }

    public NetworkConnection getConnection() {
        return connection;
    }

    public void receive(NetworkConnection connection, Object object){
        this.connection = connection;
    }

    public void onConnect(NetworkConnection connection){

    }

    public void onDisconnect(NetworkConnection connection){

    }
}
