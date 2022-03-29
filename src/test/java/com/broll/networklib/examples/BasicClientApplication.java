package com.broll.networklib.examples;

import com.broll.networklib.client.GameClient;

public class BasicClientApplication {
    public static void main(String[] args) {
        GameClient client = new GameClient(new BasicNetworkRegistry());
        client.register(new BasicClientSite());
        client.connect("localhost");
        client.shutdown();
    }
}
