package com.broll.networklib.client.tasks.impl;

import com.broll.networklib.client.GameClient;
import com.broll.networklib.client.auth.ClientAuthenticationKey;
import com.broll.networklib.client.impl.LobbyLookupSite;
import com.broll.networklib.client.tasks.AbstractClientTask;
import com.broll.networklib.client.tasks.ServerInformation;
import com.broll.networklib.client.tasks.ServerDiscoveryResult;
import com.broll.networklib.client.tasks.ServerResult;

import java.util.ArrayList;
import java.util.List;

public class LobbyDiscoveryTask extends AbstractClientTask<ServerDiscoveryResult> {
    private GameClient basicClient;
    private String version;

    public LobbyDiscoveryTask(GameClient basicClient, ClientAuthenticationKey key, String version) {
        super(key);
        this.version = version;
        this.basicClient = basicClient;
    }

    @Override
    protected void run() {
        List<ServerInformation> discoveredLobbies = new ArrayList<>();
        List<String> servers = basicClient.discoverServers();
        //also check localhost for servers
        servers.add("localhost");
        //lookup lobbies in each server
        servers.forEach(server -> {
            LobbyLookupSite site = new LobbyLookupSite();
            runOnTempClient(server, site);
            site.lookup(authKey, version);
            ServerResult result = waitFor(site.getFuture());
            if(result.getReconnectedLobby()!=null){
                complete(new ServerDiscoveryResult(result.getReconnectedLobby()));
            }
            discoveredLobbies.add(result.getServer());
        });
        complete(new ServerDiscoveryResult(discoveredLobbies));
    }
}
