package com.broll.networklib.client.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.auth.ClientAuthenticationKey;
import com.broll.networklib.client.tasks.AbstractTaskSite;
import com.broll.networklib.client.tasks.DiscoveredLobbies;
import com.broll.networklib.network.nt.NT_ListLobbies;
import com.broll.networklib.network.nt.NT_ServerInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyLookupSite extends AbstractTaskSite<DiscoveredLobbies> {

    private final static Logger Log = LoggerFactory.getLogger(LobbyLookupSite.class);



    public void lookup(ClientAuthenticationKey key) {
        Log.info("SEND LOOKUP");
        NT_ListLobbies nt = new NT_ListLobbies();
        nt.authenticationKey = key.getSecret();
        client.sendTCP(nt);
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