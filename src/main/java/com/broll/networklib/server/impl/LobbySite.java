package com.broll.networklib.server.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.nt.NT_ChatMessage;
import com.broll.networklib.network.nt.NT_LobbyKick;
import com.broll.networklib.server.ConnectionRestriction;
import com.broll.networklib.server.LobbyServerSite;
import com.broll.networklib.server.RestrictionType;

public class LobbySite<L extends ILobbyData, P extends ILobbyData> extends LobbyServerSite<L, P> {

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.IN_LOBBY)
    public void receive(NT_ChatMessage chatMessage) {
        //forward message to all other players in the lobby
        chatMessage.from = getPlayer().getName();
        getLobby().getPlayers().stream().filter(p -> p != getPlayer()).forEach(p -> p.sendTCP(chatMessage));
    }

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.LOBBY_UNLOCKED)
    public void receive(NT_LobbyKick kick) {
        ServerLobby<L, P> lobby = getLobby();
        if (lobby.getOwner() == getPlayer() ) {
            Player<P> playerToKick = lobby.getPlayer(kick.player);
            if (playerToKick != null) {
                lobby.kickPlayer(playerToKick);
            }
        }
    }
}
