package com.broll.networklib.test;

import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.GameServer;
import com.broll.networklib.server.NetworkConnection;
import com.esotericsoftware.kryonet.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class TestServer extends GameServer {
    private final static Logger Log = LoggerFactory.getLogger(TestServer.class);
    private List<ReceivedPackage> received = new ArrayList<>();
    private int timeout;

    public TestServer(IRegisterNetwork registerNetwork, int timeout) {
        super(registerNetwork);
        this.timeout = timeout;
    }

    public <T> T assureReceived(Class<T> type, NetworkConnection from) {
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

    @Override
    public void shutdown() {
        super.shutdown();

    }

    public void dropReceivedPackages() {
        received.clear();
    }

    @Override
    protected void received(NetworkConnection connection, Object o) {
        Log.info("server received "+o.getClass().getSimpleName());
        super.received(connection, o);
        ReceivedPackage pkg = new ReceivedPackage();
        pkg.from = connection;
        pkg.pkg = o;
        received.add(pkg);
    }

    private class ReceivedPackage {
        public Object pkg;
        public NetworkConnection from;
    }

}
