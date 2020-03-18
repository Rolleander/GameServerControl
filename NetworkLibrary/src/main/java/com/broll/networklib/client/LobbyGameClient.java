package com.broll.networklib.client;

import com.broll.networklib.GameEndpoint;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.LobbyConnectionSite;
import com.broll.networklib.client.impl.LobbyLookupSite;
import com.broll.networklib.network.NetworkRequest;
import com.broll.networklib.network.NetworkRequestAttempt;

import java.util.List;

public class LobbyGameClient {

    private GameClient client;
    private LobbyConnectionSite lobbyConnectionSite = new LobbyConnectionSite();

    public LobbyGameClient() {
        client = new GameClient();
        register(lobbyConnectionSite);
    }

    public void register(ClientSite... sites) {
        client.register(sites);
    }

    public void listLobbies(NetworkRequest<GameLobby> request) {
        List<String> servers = client.discoverServers();
        //also check localhost for servers
        servers.add("localhost");
        //lookup lobbies in each server
        servers.forEach(server ->
                LobbyLookupSite.openLobbyLookupClient(server, request)
        );
    }

    public void clearClientAuthenticationKey(){
        ClientAuthenticationKey.clearFileCache();
    }

    public void connectToLobby(GameLobby lobby, String playerName, NetworkRequestAttempt<GameLobby> request) {
        GameEndpoint.attemptRequest(request, () -> {
            client.connect(lobby.getServerIp());
            lobbyConnectionSite.tryJoinLobby(lobby, playerName, ClientAuthenticationKey.fromFileCache(), request);
        });
    }

    public void shutdown(){
        client.shutdown();
    }
}
