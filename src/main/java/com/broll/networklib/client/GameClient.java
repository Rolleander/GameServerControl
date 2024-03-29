package com.broll.networklib.client;

import com.broll.networklib.GameEndpoint;
import com.broll.networklib.ThreadedListener;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.network.NetworkException;
import com.broll.networklib.network.NetworkRegistry;
import com.broll.networklib.site.SingleSitesHandler;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

public class GameClient extends GameEndpoint<ClientSite, GameClient.ClientConnection> {

    private final static Logger Log = LoggerFactory.getLogger(GameClient.class);
    private final static int CONNECTION_TIMEOUT = 5000;
    private Client client = new Client(GameEndpoint.WRITE_BUFFER_SIZE, GameEndpoint.OBJECT_BUFFER_SIZE);
    private String connectedIp;
    private final ClientConnection connection = new ClientConnection();
    private final ThreadedListener threadedListener;

    public GameClient(IRegisterNetwork registerNetwork) {
        super(registerNetwork, new SingleSitesHandler<>());
        init();
        this.threadedListener = new ThreadedListener(new ClientListener(), "Client-worker", 1);
        this.threadedListener.attach(client);
    }

    public void connect(String ip) {
        if (isConnected()) {
            if (StringUtils.equals(ip, connectedIp)) {
                return;
            }
            shutdown();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.error("Failed sleeping", e);
            }
        }
        try {
            client.start();
            client.connect(CONNECTION_TIMEOUT, ip, NetworkRegistry.TCP_PORT, NetworkRegistry.UDP_PORT);
            Log.info("Client connected to server " + ip);
        } catch (IOException e) {
            throw new NetworkException("Failed to connect with server " + ip, e);
        }
    }

    public List<String> discoverServers() {
        return client.discoverHosts(NetworkRegistry.TCP_PORT, NetworkRegistry.UDP_PORT).stream().map(InetAddress::toString).collect(Collectors.toList());
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public void shutdown() {
        client.stop();
        try {
            client.dispose();
        } catch (IOException e) {
            Log.error("Failed to dispose client", e);
        }
        threadedListener.remove(client);
    }

    @Override
    public Kryo getKryo() {
        return client.getKryo();
    }

    public void sendTCP(Object object) {
        client.sendTCP(object);
    }

    public void sendUDP(Object object) {
        client.sendUDP(object);
    }

    public String getConnectedIp() {
        return connectedIp;
    }

    private class ClientListener extends Listener {
        @Override
        public void connected(Connection c) {
            connectedIp = c.getRemoteAddressTCP().getHostName();
            initConnection(connection);
            passAllSites(connection, sites -> sites.forEach(site -> site.onConnect()));
        }

        @Override
        public void disconnected(Connection c) {
            passAllSites(connection, sites -> sites.forEach(site -> site.onDisconnect()));
            connectedIp = null;
            discardConnection(connection);
        }

        @Override
        public void received(Connection c, Object o) {
            if(o instanceof FrameworkMessage){
                return;
            }
            GameClient.this.received(o);
        }
    }

    protected void received(Object o) {
        passReceived(connection, o, sites -> sites.forEach(site -> site.receive(o)));
    }

    public class ClientConnection {
        //empty dummy since clients do not have multiple connections
    }
}
