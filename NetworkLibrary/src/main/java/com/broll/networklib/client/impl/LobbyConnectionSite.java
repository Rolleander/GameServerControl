package com.broll.networklib.client.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.LobbyClientSite;
import com.broll.networklib.network.nt.NT_ChatMessage;
import com.broll.networklib.network.nt.NT_LobbyClosed;
import com.broll.networklib.network.nt.NT_LobbyJoined;
import com.broll.networklib.network.nt.NT_LobbyUpdate;
import com.broll.networklib.network.nt.NT_LobbyKicked;

public class LobbyConnectionSite extends LobbyClientSite {

    private GameLobby lobby;
    private Runnable clearLobby;

    public LobbyConnectionSite(Runnable clearLobby) {
        this.clearLobby = clearLobby;
    }

    public void setLobby(GameLobby lobby) {
        this.lobby = lobby;
    }

    @PackageReceiver
    public void receive(NT_LobbyUpdate lobbyUpdate) {
        if (lobbyUpdate instanceof NT_LobbyJoined) {
            //handled in joining site
            return;
        }
        LobbyChange.updateLobby(lobby, lobbyUpdate);
        LobbyUpdateListener listener = lobby.getLobbyUpdateListener();
        if (listener != null) {
            listener.lobbyUpdated();
        }
    }

    @PackageReceiver
    public void receive(NT_ChatMessage chat) {
        if (lobby != null) {
            ChatMessageListener listener = lobby.getChatMessageListener();
            if (listener != null) {
                if (chat.from == null) {
                    //message from system
                    listener.fromGame(chat.message);
                } else {
                    //from player
                    lobby.getPlayer(chat.from).ifPresent(player -> listener.fromPlayer(chat.message, player));
                }
            }
        }
    }

    @PackageReceiver
    public void receive(NT_LobbyKicked kicked) {
        //player was removed
        if (lobby != null && lobby.getLobbyUpdateListener() != null) {
            lobby.getLobbyUpdateListener().kickedFromLobby();
        }
        resetLobby();
    }

    @PackageReceiver
    public void receive(NT_LobbyClosed closed) {
        //lobby was closed
        if (lobby != null && lobby.getLobbyUpdateListener() != null) {
            lobby.getLobbyUpdateListener().closed();
        }
        resetLobby();
    }

    private synchronized void resetLobby() {
        clearLobby.run();
        lobby = null;
    }

}
