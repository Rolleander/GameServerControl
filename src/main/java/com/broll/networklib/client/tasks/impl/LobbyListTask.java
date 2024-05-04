package com.broll.networklib.client.tasks.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.auth.ClientAuthenticationKey;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.LobbyChange;
import com.broll.networklib.client.impl.LobbyLookupSite;
import com.broll.networklib.client.tasks.AbstractClientTask;
import com.broll.networklib.client.tasks.AbstractTaskSite;
import com.broll.networklib.client.tasks.DiscoveredLobbies;
import com.broll.networklib.client.tasks.LobbyListResult;
import com.broll.networklib.network.nt.NT_ServerInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyListTask extends AbstractClientTask<LobbyListResult> {
    private final static Logger Log = LoggerFactory.getLogger(LobbyLookupSite.class);

    private String ip;


    private String version;

    public LobbyListTask(ClientAuthenticationKey key,  String version) {
        this(null, key, version);
    }

    public LobbyListTask(String ip, ClientAuthenticationKey key,  String version) {
        super(key);
        this.ip = ip;
        this.version = version;
    }

    @Override
    protected void run() {
        LobbyLookupSite site = new LobbyLookupSite();
        if (ip == null) {
            runOnConnectedClient(site);
        } else {
            runOnClient(ip, site);
        }
        site.lookup(authKey, version);
        complete(waitFor(site.getFuture()));
    }

}
