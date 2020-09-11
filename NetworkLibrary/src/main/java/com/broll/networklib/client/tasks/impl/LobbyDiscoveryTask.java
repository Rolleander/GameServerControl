package com.broll.networklib.client.tasks.impl;

import com.broll.networklib.client.GameClient;
import com.broll.networklib.client.impl.LobbyLookupSite;
import com.broll.networklib.client.tasks.AbstractClientTask;
import com.broll.networklib.client.tasks.DiscoveredLobbies;

import java.util.ArrayList;
import java.util.List;

public class LobbyDiscoveryTask extends AbstractClientTask<List<DiscoveredLobbies>> {
    private GameClient basicClient;

    public LobbyDiscoveryTask(GameClient basicClient) {
        this.basicClient = basicClient;
    }

    @Override
    protected void run() {
        List<DiscoveredLobbies> discoveredLobbies = new ArrayList<>();
        List<String> servers = basicClient.discoverServers();
        //also check localhost for servers
        servers.add("localhost");
        //lookup lobbies in each server
        servers.forEach(server -> {
            LobbyLookupSite site = new LobbyLookupSite();
            runOnTempClient(server, site);
            site.lookup();
            DiscoveredLobbies lobbies = waitFor(site.getFuture());
            discoveredLobbies.add(lobbies);
        });
        complete(discoveredLobbies);
    }
}
