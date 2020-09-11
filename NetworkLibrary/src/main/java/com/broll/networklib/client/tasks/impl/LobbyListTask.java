package com.broll.networklib.client.tasks.impl;

import com.broll.networklib.client.impl.LobbyLookupSite;
import com.broll.networklib.client.tasks.AbstractClientTask;
import com.broll.networklib.client.tasks.DiscoveredLobbies;

public class LobbyListTask extends AbstractClientTask<DiscoveredLobbies> {

    private String ip;

    public LobbyListTask() {
        this(null);
    }

    public LobbyListTask(String ip) {
        this.ip = ip;
    }

    @Override
    protected void run() {
        LobbyLookupSite site = new LobbyLookupSite();
        if (ip == null) {
            runOnConnectedClient(site);
        } else {
            runOnClient(ip, site);
        }
        site.lookup();
        complete(waitFor(site.getFuture()));
    }
}
