package com.broll.networklib.server;

import com.broll.networklib.GameEndpoint;
import com.broll.networklib.ThreadedListener;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.network.NetworkException;
import com.broll.networklib.network.NetworkRegistry;
import com.broll.networklib.network.nt.NT_ReconnectCheck;
import com.broll.networklib.site.MultiSitesHandler;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class GameServer extends GameEndpoint<ServerSite, NetworkConnection> {

    private final static Logger Log = LoggerFactory.getLogger(GameServer.class);
    private Server server = new Server(GameEndpoint.WRITE_BUFFER_SIZE * 2, GameEndpoint.OBJECT_BUFFER_SIZE) {
        @Override
        protected Connection newConnection() {
            return new NetworkConnection();
        }
    };
    private boolean open = false;
    private ThreadedListener threadedListener;
    private final static int DEFAULT_POOLSIZE = 10;

    public GameServer(IRegisterNetwork registerNetwork) {
        this(registerNetwork, new ServerSitesHandler());
    }

    public GameServer(IRegisterNetwork registerNetwork, MultiSitesHandler sitesHandler) {
        super(registerNetwork, sitesHandler);
        init();
    }

    public void open() {
        open(DEFAULT_POOLSIZE);
    }

    public void open(int threadPoolSize) {
        if (open) {
            shutdown();
        }
        threadedListener = new ThreadedListener(new ConnectionListener(), "Server-worker-%d", threadPoolSize);
        threadedListener.attach(server);
        server.start();
        try {
            server.bind(NetworkRegistry.TCP_PORT, NetworkRegistry.UDP_PORT);
            Log.info("Server started");
            open = true;
        } catch (IOException e) {
            throw new NetworkException(e);
        }
    }

    @Override
    public void shutdown() {
        server.stop();
        if (threadedListener != null) {
            threadedListener.remove(server);
        }
        try {
            server.dispose();
        } catch (IOException e) {
            Log.error("Failed to dispose server", e);
        }
        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    public void sendToAllTCP(Object object) {
        server.sendToAllTCP(object);
    }

    public void sendToAllUDP(Object object) {
        server.sendToAllUDP(object);
    }

    @Override
    public Kryo getKryo() {
        return server.getKryo();
    }

    private class ConnectionListener extends Listener {
        @Override
        public void connected(Connection c) {
            Log.info(c + " connected to server");
            NetworkConnection connection = (NetworkConnection) c;
            connection.setActive(true);
            initConnection(connection);
            passAllSites(connection, sites -> sites.forEach(site -> site.onConnect(connection)));
        }

        @Override
        public void disconnected(Connection c) {
            Log.info(c + " disconnected from server");
            NetworkConnection connection = (NetworkConnection) c;
            connection.setActive(false);
            passAllSites(connection, sites -> sites.forEach(site -> site.onDisconnect(connection)));
            discardConnection(connection);
        }

        @Override
        public void received(Connection c, Object o) {
            NetworkConnection connection = (NetworkConnection) c;
            GameServer.this.received(connection, o);
        }
    }

    protected void received(NetworkConnection connection, Object o) {
        passReceived(connection, o, sites -> sites.forEach(site -> site.receive(connection, o)));
    }

}
