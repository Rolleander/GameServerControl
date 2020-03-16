package com.broll.networklib.server;

import com.broll.networklib.GameEndpoint;
import com.broll.networklib.network.NetworkException;
import com.broll.networklib.network.NetworkRegistry;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import java.io.IOException;

public class GameServer extends GameEndpoint<ServerSite> {

    private Server server = new Server() {
        @Override
        protected Connection newConnection() {
            return new NetworkConnection();
        }
    };

    public GameServer() {
    }

    public void open() {
        server.addListener(new ConnectionListener());
        init();
        server.start();
        try {
            server.bind(NetworkRegistry.TCP_PORT, NetworkRegistry.UDP_PORT);
            Log.info("Server started");
        } catch (IOException e) {
            throw new NetworkException(e);
        }
    }

    public void shutdown() {
        server.stop();
    }

    public void sendToAllTCP(Object object){
        server.sendToAllTCP(object);
    }

    public void sendToAllUDP(Object object){
        server.sendToAllUDP(object);
    }

    @Override
    protected Kryo getKryo() {
        return server.getKryo();
    }

    private class ConnectionListener extends Listener {
        @Override
        public void connected(Connection c) {
            NetworkConnection connection = (NetworkConnection) c;
            connection.setActive(true);
            sites.getSites().forEach(site -> site.onConnect(connection));
        }

        @Override
        public void disconnected(Connection c) {
            NetworkConnection connection = (NetworkConnection) c;
            connection.setActive(false);
            sites.getSites().forEach(site -> site.onDisconnect(connection));
        }

        @Override
        public void received(Connection c, Object o) {
            NetworkConnection connection = (NetworkConnection) c;
            sites.pass(o, sites -> sites.forEach(site -> site.receive(connection, o)));
        }
    }

}
