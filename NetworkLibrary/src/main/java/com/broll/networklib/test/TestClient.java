package com.broll.networklib.test;

import java.util.ArrayList;
import java.util.List;

import com.broll.networklib.client.GameClient;
import com.broll.networklib.network.IRegisterNetwork;
import com.google.common.util.concurrent.SettableFuture;

public class TestClient extends GameClient {

    private List<Object> receivedObjects = new ArrayList<>();
    private int timeout;

    public TestClient(IRegisterNetwork registerNetwork, int timeout) {
        super(registerNetwork);
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
    protected void received(Object o) {
        super.received(o);
        receivedObjects.add(o);
    }
}
