package com.broll.networklib.client.tasks.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.auth.ClientAuthenticationKey;
import com.broll.networklib.client.auth.LastConnection;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.LobbyChange;
import com.broll.networklib.client.tasks.AbstractClientTask;
import com.broll.networklib.client.tasks.AbstractTaskSite;
import com.broll.networklib.network.nt.NT_LobbyJoin;
import com.broll.networklib.network.nt.NT_LobbyJoined;
import com.broll.networklib.network.nt.NT_LobbyNoJoin;

public class JoinLobbyTask extends AbstractClientTask<GameLobby> {

    private GameLobby lobby;
    private String playerName;

    public JoinLobbyTask(GameLobby lobby, String playerName, ClientAuthenticationKey authKey) {
        super(authKey);
        this.lobby = lobby;
        this.playerName = playerName;
    }

    @Override
    protected void run() {
        JoinLobbySite site = new JoinLobbySite();
        runOnConnectedClient(site);
        site.join();
        complete(waitFor(site.getFuture()));
    }

    private class JoinLobbySite extends AbstractTaskSite<GameLobby> {

        public void join() {
            NT_LobbyJoin join = new NT_LobbyJoin();
            join.lobbyId = lobby.getLobbyId();
            join.authenticationKey = authKey.getSecret();
            join.playerName = playerName;
            client.sendTCP(join);
        }

        @PackageReceiver
        public void receive(NT_LobbyJoined lobbyJoin) {
            LobbyChange.joinedLobby(getClient(),lobby, lobbyJoin);
            complete(lobby);
        }

        @PackageReceiver
        public void receive(NT_LobbyNoJoin noJoin) {
            fail("Could not join lobby: " + noJoin.reason);
        }

    }
}
