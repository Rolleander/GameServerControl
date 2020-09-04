package com.broll.networklib.server.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.nt.NT_ChatMessage;
import com.broll.networklib.server.LobbyServerSite;

public class LobbySite<L extends LobbySettings, P extends LobbySettings> extends LobbyServerSite<L, P> {

    @PackageReceiver
    public void receive(NT_ChatMessage chatMessage) {
        //forward message to all other players in the lobby
        chatMessage.from = getPlayer().getName();
        getLobby().getPlayers().stream().filter(p -> p != getPlayer()).forEach(p -> p.sendTCP(chatMessage));
    }

}
