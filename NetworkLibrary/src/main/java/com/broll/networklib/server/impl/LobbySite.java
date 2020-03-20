package com.broll.networklib.server.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.nt.NT_ChatMessage;
import com.broll.networklib.server.LobbyServerSite;

public class LobbySite<L, P> extends LobbyServerSite<L, P> {

    @PackageReceiver
    public void receive(NT_ChatMessage chatMessage) {

    }

}
