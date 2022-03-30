package com.broll.networklib.examples.basic;

import com.broll.networklib.server.GameServer;

public class BasicServerApplication {
    public static void main(String[] args) {
        GameServer server = new GameServer(new BasicNetworkRegistry());
        server.open();
        server.register(new BasicServerSite());
    }
}
