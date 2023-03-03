package com.broll.networklib.test;

import java.util.ArrayList;
import java.util.List;

import com.broll.networklib.client.GameClient;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.impl.ConnectionSite;
import com.google.common.util.concurrent.SettableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClient extends GameClient {
    private final static Logger Log = LoggerFactory.getLogger(TestClient.class);
    private List<Object> receivedObjects = new ArrayList<>();
    private int timeout;

    private String name;

    public TestClient(String name ,IRegisterNetwork registerNetwork, int timeout) {
        super(registerNetwork);
        this.name = name;
        this.timeout = timeout;
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
    public void sendTCP(Object object) {
        Log.info(name+" sends "+object.getClass().getSimpleName());
        super.sendTCP(object);
    }

    @Override
    public void sendUDP(Object object) {
        Log.info(name+" sends "+object.getClass().getSimpleName());
        super.sendUDP(object);
    }

    @Override
    protected void received(Object o) {
        Log.info(name+" received "+o.getClass().getSimpleName());
        super.received(o);
        receivedObjects.add(o);
    }
}
