package com.broll.networklib.test;

import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.GameServer;
import com.broll.networklib.server.NetworkConnection;
import com.esotericsoftware.kryonet.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class TestServer extends GameServer {

    private Map<TestClient, TestConnection> connections = new HashMap<>();
    private List<ReceivedPackage> received = new ArrayList<>();
    private int timeout;

    public TestServer(IRegisterNetwork registerNetwork,int timeout) {
        super(registerNetwork);
        this.timeout = timeout;
    }

    @Override
    public void open() {
        //do nothing
    }

    @Override
    public void shutdown() {
        //do nothing
    }

    void connected(TestClient client) {
        TestConnection connection = new TestConnection(client);
        connections.put(client, connection);
    }

    public <T> T assureReceived(Class<T> type, TestClient from) {
        ReceivedPackage pkg = (ReceivedPackage) TestUtils.poll(received, timeout);
        if (pkg == null) {
            throw new RuntimeException("No message received");
        }
        if (pkg.from != from) {
            throw new RuntimeException("Expected to receive message from client " + from + ", but received from " + pkg.from);
        }
        if (!type.isInstance(pkg.pkg)) {
            throw new RuntimeException("Expected message of type " + type + ", but received " + pkg.pkg);
        }
        return (T) pkg.pkg;
    }

    public NetworkConnection getConnection(TestClient client){
        return connections.get(client);
    }

    public void dropReceivedPackages(){
        received.clear();
    }

    void receive(TestClient client, Object o) {
        System.out.println("server received "+o+" from client "+client);
        received(connections.get(client), o);
        ReceivedPackage pkg = new ReceivedPackage();
        pkg.from = client;
        pkg.pkg = o;
        received.add(pkg);
    }

    @Override
    public void sendToAllTCP(Object object) {
        connections.values().forEach(con -> con.sendTCP(object));
    }

    @Override
    public void sendToAllUDP(Object object) {
        connections.values().forEach(con -> con.sendUDP(object));
    }

    private class ReceivedPackage {
        public Object pkg;
        public TestClient from;
    }

    private class TestConnection extends NetworkConnection {
        private TestClient client;

        public TestConnection(TestClient client) {
            this.client = client;
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public int sendTCP(Object o) {
            client.receive(o);
            return 0;
        }

        @Override
        public int sendUDP(Object o) {
            client.receive(o);
            return 0;
        }
    }
}
