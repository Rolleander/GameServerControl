package com.broll.networklib.server.impl;

import com.broll.networklib.server.NetworkConnection;

public class ConnectionSite extends AbstractLobbyServerSite {

    public ConnectionSite(LobbyHandler lobbyHandler) {
        super(lobbyHandler);
    }

    @Override
    public void onConnect(NetworkConnection connection) {
        //send updates for lobbies

    }

    @Override
    public void onDisconnect(NetworkConnection connection) {
        Player player = connection.getPlayer();
        if(player!=null) {
            player.updateOnlineStatus(false);
            if (player.inLobby()) {
                ServerLobby lobby = player.getServerLobby();
                if (!lobby.isGameStarted()) {
                    //remove from lobby
                }
            }
        }
    }
}
