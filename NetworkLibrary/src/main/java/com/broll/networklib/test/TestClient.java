package com.broll.networklib.test;

import java.util.ArrayList;
import java.util.List;

import com.broll.networklib.client.GameClient;
import com.broll.networklib.network.IRegisterNetwork;
import com.google.common.util.concurrent.SettableFuture;

public class TestClient extends GameClient {

    private TestServer server;
    private List<Object> receivedObjects = new ArrayList<>();
    private int timeout;
    private boolean connected = false;

    public TestClient(IRegisterNetwork registerNetwork, int timeout) {
        super(registerNetwork);
        this.timeout = timeout;
    }

    @Override
    public void connect(String ip) {
        //do nothing
    }

    @Override
    public void shutdown() {
        //do nothing
        disconnect(server);
    }

    public void connect(TestServer server) {
        this.server = server;
        this.connected = true;

        server.connected(this);
    }

    public void disconnect(TestServer server) {
        server.disconnect(this);
    }

    public void dropReceivedPackages() {
        receivedObjects.clear();
    }

    public <T> T assureReceived(Class<T> type) {
        Object pkg = TestUtils.poll(receivedObjects, timeout);
        if (pkg == null) {
            throw new RuntimeException("No message received");
        }
        if (!type.isInstance(pkg)) {
            throw new RuntimeException("Expected message of type " + type + ", but received " + pkg);
        }
        return (T) pkg;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void sendTCP(Object object) {
        server.receive(this, object);
    }

    @Override
    public void sendUDP(Object object) {
        server.receive(this, object);
    }

    void receive(Object object) {
        this.received(object);
        receivedObjects.add(object);
    }
}
