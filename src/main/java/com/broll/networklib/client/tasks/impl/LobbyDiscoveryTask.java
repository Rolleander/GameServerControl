package com.broll.networklib.client.tasks.impl;

import com.broll.networklib.client.GameClient;
import com.broll.networklib.client.auth.ClientAuthenticationKey;
import com.broll.networklib.client.impl.LobbyLookupSite;
import com.broll.networklib.client.tasks.AbstractClientTask;
import com.broll.networklib.client.tasks.DiscoveredLobbies;
import com.broll.networklib.client.tasks.LobbyDiscoveryResult;
import com.broll.networklib.client.tasks.LobbyListResult;

import java.util.ArrayList;
import java.util.List;

public class LobbyDiscoveryTask extends AbstractClientTask<LobbyDiscoveryResult> {
    private GameClient basicClient;
    private String version;

    public LobbyDiscoveryTask(GameClient basicClient, ClientAuthenticationKey key, String version) {
        super(key);
        this.version = version;
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
            site.lookup(authKey, version);
            LobbyListResult result = waitFor(site.getFuture());
            if(result.getReconnectedLobby()!=null){
                complete(new LobbyDiscoveryResult(result.getReconnectedLobby()));
            }
            discoveredLobbies.add(result.getLobbies());
        });
        complete(new LobbyDiscoveryResult(discoveredLobbies));
    }
}
