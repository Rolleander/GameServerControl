package com.broll.networklib.server.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.nt.NT_ChatMessage;
import com.broll.networklib.network.nt.NT_LobbyKick;
import com.broll.networklib.network.nt.NT_LobbyLeave;
import com.broll.networklib.server.ConnectionRestriction;
import com.broll.networklib.server.LobbyServerSite;
import com.broll.networklib.server.RestrictionType;

public class LobbySite<L extends ILobbyData, P extends ILobbyData> extends LobbyServerSite<L, P> {

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.IN_LOBBY)
    private void receive(NT_ChatMessage chatMessage) {
        chat(chatMessage);
    }

    protected void chat(NT_ChatMessage chatMessage){
        chatMessage.from = getPlayer().getName();
        getLobby().getActivePlayers().stream().filter(p -> p != getPlayer()).forEach(p -> p.sendTCP(chatMessage));
    }

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.LOBBY_UNLOCKED)
    private void receive(NT_LobbyKick kick) {
      kick(kick);
    }

    protected void kick(NT_LobbyKick kick){
        ServerLobby<L, P> lobby = getLobby();
        if (lobby.getOwner() == getPlayer() ) {
            Player<P> playerToKick = lobby.getPlayer(kick.player);
            if (playerToKick != null) {
                lobby.kickPlayer(playerToKick);
            }
        }
    }

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.LOBBY_UNLOCKED)
    private void receive(NT_LobbyLeave nt) {
       leave(nt);
    }

    protected void leave(NT_LobbyLeave leave){
        ServerLobby<L, P> lobby = getLobby();
        lobby.getLobbyHandler().removePlayer(lobby, getPlayer());
    }
}
