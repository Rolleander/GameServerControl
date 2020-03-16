package com.broll.networklib.client.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.ClientSite;
import com.broll.networklib.client.GameClient;
import com.broll.networklib.network.NetworkRequest;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_ServerInformation;
import com.esotericsoftware.minlog.Log;

public class LobbyLookupSite extends ClientSite {

    private NetworkRequest<GameLobby> request;

    public LobbyLookupSite(NetworkRequest<GameLobby> request) {
        this.request = request;
    }

    public static void openLobbyLookupClient(String ip, NetworkRequest<GameLobby> request){
        try {
            GameClient lookupClient = new GameClient();
            lookupClient.register(new LobbyLookupSite(request));
            lookupClient.connect(ip);
        } catch (Exception e){
            Log.error("Failed to open lobby lookup client on ip "+ip);
        }
    }

    @PackageReceiver
    public void receive(NT_ServerInformation info) {
        for (NT_LobbyInformation lobbyInfo : info.lobbies) {
            GameLobby lobby = new GameLobby();
            lobby.setServerIp(getClient().getConnectedIp());
            updateLobbyInfo(lobby, lobbyInfo);
            request.receive(lobby);
        }
        client.shutdown();
    }

    public static void updateLobbyInfo(GameLobby lobby, NT_LobbyInformation lobbyInfo){
        lobby.setName(lobbyInfo.lobbyName);
        lobby.setLobbyId(lobbyInfo.lobbyId);
        lobby.setPlayerCount(lobbyInfo.playerCount);
        lobby.setPlayerLimit(lobbyInfo.playerLimit);
    }
}