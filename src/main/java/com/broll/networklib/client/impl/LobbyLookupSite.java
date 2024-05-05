package com.broll.networklib.client.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.auth.ClientAuthenticationKey;
import com.broll.networklib.client.tasks.AbstractTaskSite;
import com.broll.networklib.client.tasks.ServerInformation;
import com.broll.networklib.client.tasks.ServerResult;
import com.broll.networklib.network.nt.NT_ListLobbies;
import com.broll.networklib.network.nt.NT_LobbyNoJoin;
import com.broll.networklib.network.nt.NT_LobbyReconnected;
import com.broll.networklib.network.nt.NT_ServerInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyLookupSite extends AbstractTaskSite<ServerResult> {

    private final static Logger Log = LoggerFactory.getLogger(LobbyLookupSite.class);

    public void lookup(ClientAuthenticationKey key, String version) {
        Log.info("SEND LOOKUP");
        NT_ListLobbies nt = new NT_ListLobbies();
        nt.authenticationKey = key.getSecret();
        nt.version = version;
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
        complete(new ServerResult(new ServerInformation(info.serverName, ip, lobbies)));
    }

    @PackageReceiver
    public void reconnected(NT_LobbyReconnected reconnected) {
        complete(new ServerResult(LobbyChange.reconnectedLobby(getClient(), reconnected)));
    }

    @PackageReceiver
    public void wrongVersion(NT_LobbyNoJoin nt) {
        fail("Could not list lobbies: " + nt.reason);
    }

}