package com.broll.networklib.client.tasks.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.auth.ClientAuthenticationKey;
import com.broll.networklib.client.auth.LastConnection;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.LobbyChange;
import com.broll.networklib.client.tasks.AbstractClientTask;
import com.broll.networklib.client.tasks.AbstractTaskSite;
import com.broll.networklib.network.nt.NT_LobbyCreate;
import com.broll.networklib.network.nt.NT_LobbyJoined;
import com.broll.networklib.network.nt.NT_LobbyNoJoin;

public class CreateLobbyTask extends AbstractClientTask<GameLobby> {

    private String playerName;
    private Object settings;

    private String version;


    public CreateLobbyTask(String playerName, Object lobbySettings, ClientAuthenticationKey authKey, String version) {
        super(authKey);
        this.playerName = playerName;
        this.settings = lobbySettings;
        this.version = version;
    }

    @Override
    protected void run() {
        CreateLobbySite site = new CreateLobbySite();
        runOnConnectedClient(site);
        site.create();
        complete(waitFor(site.getFuture()));
    }

    private class CreateLobbySite extends AbstractTaskSite<GameLobby> {

        public void create() {
            NT_LobbyCreate create = new NT_LobbyCreate();
            create.playerName = playerName;
            create.lobbyName = playerName + "'s Lobby";
            create.authenticationKey = authKey.getSecret();
            create.settings = settings;
            create.version = version;
            client.sendTCP(create);
        }

        @PackageReceiver
        public void receive(NT_LobbyJoined lobbyJoin) {
            GameLobby lobby = LobbyChange.createdLobby(getClient(), lobbyJoin, getClient().getConnectedIp());
            complete(lobby);
        }

        @PackageReceiver
        public void receive(NT_LobbyNoJoin noJoin) {
            fail("Could not create lobby: " + noJoin.reason);
        }

    }
}
