package com.broll.networklib.examples.lobby.client;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.LobbyClientSite;
import com.broll.networklib.examples.lobby.nt.NT_StartGame;

public class MonopolyClientSite extends LobbyClientSite {

    @PackageReceiver
    public void received(NT_StartGame startGame){
        getPlayer(); // player information
        getLobby(); // lobby information
        getClient(); // client access
    }

}
