package com.broll.networklib.client;

import com.broll.networklib.GameEndpoint;
import com.broll.networklib.NetworkException;
import com.broll.networklib.network.NetworkRegistry;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

public class GameClient extends GameEndpoint<ClientSite> {

    private final static int CONNECTION_TIMEOUT = 5000;
    private Client client = new Client();

    public void connect(String ip) {
        try {
            client.addListener(new ClientListener());
            init();
            client.start();
            client.connect(CONNECTION_TIMEOUT, ip, NetworkRegistry.TCP_PORT, NetworkRegistry.UDP_PORT);
            Log.info("Client connected to server "+ip);
        } catch (IOException e) {
            throw new NetworkException("Failed to connect to server", e);
        }
    }

    public List<String> discoverServers(){
        return client.discoverHosts(NetworkRegistry.TCP_PORT, NetworkRegistry.UDP_PORT).stream().map(InetAddress::toString).collect(Collectors.toList());
    }

    @Override
    public void shutdown() {
        client.stop();
    }

    @Override
    protected Kryo getKryo() {
        return client.getKryo();
    }

    public void sendTCP(Object object){
        client.sendTCP(object);
    }

    public void sendUDP(Object object){
        client.sendUDP(object);
    }

    private class ClientListener extends Listener {
        @Override
        public void connected(Connection c) {
            sites.getSites().forEach(site -> site.onConnect());
        }

        @Override
        public void disconnected(Connection c) {
            sites.getSites().forEach(site -> site.onDisconnect());
        }

        @Override
        public void received(Connection c, Object o) {
            sites.pass(o, sites -> sites.forEach(site -> site.receive(o)));
        }
    }
}
