package com.broll.networklib.server.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.nt.NT_ChatMessage;

public class LobbySite extends AbstractLobbyServerSite {

    public LobbySite(LobbyHandler lobbyHandler) {
        super(lobbyHandler);
    }

    @PackageReceiver
    public void receive(NT_ChatMessage chatMessage){

    }
}
