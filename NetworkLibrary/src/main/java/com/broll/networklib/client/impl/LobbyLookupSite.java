package com.broll.networklib.client.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.tasks.AbstractTaskSite;
import com.broll.networklib.client.tasks.DiscoveredLobbies;
import com.broll.networklib.network.nt.NT_ServerInformation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyLookupSite extends AbstractTaskSite<DiscoveredLobbies> {

    public void lookup() {
        client.sendTCP(new NT_ServerInformation());
    }

    @PackageReceiver
    public void receive(NT_ServerInformation info) {
        String ip = getClient().getConnectedIp();
        List<GameLobby> lobbies = Arrays.stream(info.lobbies).map(lobbyInfo -> {
            GameLobby lobby = new GameLobby();
            lobby.setServerIp(ip);
            LobbyChange.updateLobbyInfo(lobby, lobbyInfo);
            return lobby;
        }).collect(Collectors.toList());
        complete(new DiscoveredLobbies(info.serverName, ip, lobbies));
    }

}