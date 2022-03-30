package com.broll.networklib.test.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.ClientSite;
import com.broll.networklib.client.GameClient;
import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.server.GameServer;
import com.broll.networklib.server.ServerSite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SiteTests {

    private GameServer server;
    private GameClient client;

    private static class TestPackage {

    }

    @Before
    public void before() {
        IRegisterNetwork registerNetwork = register -> {
            register.registerNetworkType(TestPackage.class);
        };
        server = new GameServer(registerNetwork);
        server.open();
        client = new GameClient(registerNetwork);
        client.connect("localhost");
    }

    @After
    public void after() {
        client.shutdown();
        server.shutdown();
    }

    @Test
    public void uknownMessageReceiver() throws ExecutionException, InterruptedException {
        CompletableFuture future = new CompletableFuture();
        client.setUnknownMessageReceiver(message -> {
            future.complete(message);
        });
        server.sendToAllTCP(new TestPackage());
        assertEquals(TestPackage.class, future.get().getClass());
        CompletableFuture future2 = new CompletableFuture();
        server.setUnknownMessageReceiver(message -> {
            future2.complete(message);
        });
        client.sendTCP(new TestPackage());
        assertEquals(TestPackage.class, future2.get().getClass());
    }

    @Test
    public void clientSite() throws ExecutionException, InterruptedException {
        CompletableFuture futureUnknown = new CompletableFuture();
        CompletableFuture futureMessage = new CompletableFuture();
        client.setUnknownMessageReceiver(message -> {
            futureUnknown.complete(message);
        });
        ClientSite site = new ClientSite() {
            @PackageReceiver
            public void received(TestPackage testPackage) {
                futureMessage.complete(testPackage);
            }
        };
        client.register(site);
        server.sendToAllTCP(new TestPackage());
        assertEquals(TestPackage.class, futureMessage.get().getClass());
        client.unregister(site);
        server.sendToAllTCP(new TestPackage());
        assertEquals(TestPackage.class, futureUnknown.get().getClass());
    }

    @Test
    public void serverSite() throws ExecutionException, InterruptedException {
        CompletableFuture futureUnknown = new CompletableFuture();
        CompletableFuture futureMessage = new CompletableFuture();
        server.setUnknownMessageReceiver(message -> {
            futureUnknown.complete(message);
        });
        ServerSite site = new TestServerSite(futureMessage);
        server.register(site);
        client.sendTCP(new TestPackage());
        assertEquals(TestPackage.class, futureMessage.get().getClass());
        server.unregister(site);
        client.sendTCP(new TestPackage());
        assertEquals(TestPackage.class, futureUnknown.get().getClass());
    }

    private static class TestServerSite extends ServerSite {
        private CompletableFuture futureMessage;

        public TestServerSite() {
            super();
        }

        public TestServerSite(CompletableFuture futureMessage) {
            this.futureMessage = futureMessage;
        }

        @PackageReceiver
        public void received(TestPackage testPackage) {
            futureMessage.complete(testPackage);
        }
    }
}
