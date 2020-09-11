package com.broll.networklib.client.tasks.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.auth.ClientAuthenticationKey;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.LobbyChange;
import com.broll.networklib.client.tasks.AbstractClientTask;
import com.broll.networklib.client.tasks.AbstractTaskSite;
import com.broll.networklib.network.nt.NT_LobbyNoJoin;
import com.broll.networklib.network.nt.NT_LobbyReconnected;
import com.broll.networklib.network.nt.NT_ReconnectCheck;

public class ReconnectTask extends AbstractClientTask<GameLobby> {

    private String ip;
    private ClientAuthenticationKey authenticationKey;

    public ReconnectTask(String ip, ClientAuthenticationKey authenticationKey) {
        this.ip = ip;
        this.authenticationKey = authenticationKey;
    }

    @Override
    protected void run() {
        ReconnectSite site = new ReconnectSite();
        runOnClient(ip, site);
        site.reconnectCheck();
        complete(waitFor(site.getFuture()));
    }

    private class ReconnectSite extends AbstractTaskSite<GameLobby> {

        public void reconnectCheck() {
            NT_ReconnectCheck reconnect = new NT_ReconnectCheck();
            reconnect.authenticationKey = authenticationKey.getSecret();
            client.sendTCP(reconnect);
        }

        @PackageReceiver
        public void reconnected(NT_LobbyReconnected reconnected) {
            complete(LobbyChange.reconnectedLobby(reconnected));
        }

        @PackageReceiver
        public void notReconnected(NT_LobbyNoJoin notReconnected) {
            fail("Could not reconnect player");
        }
    }

}
