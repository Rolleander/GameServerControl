package com.broll.networklib.examples.lobby.server;

import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ServerLobby;
import com.broll.networklib.server.impl.IServerLobbyListener;

public class MonopolyLobbyListener implements IServerLobbyListener<MonopolyLobbyData, MonopolyPlayerData> {
    @Override
    public void playerJoined(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby, Player<MonopolyPlayerData> player) {
        MonopolyPlayerData playerData = new MonopolyPlayerData();
        player.setData(playerData);
        playerData.assignFreeToken(lobby);
    }

    @Override
    public void playerLeft(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby, Player<MonopolyPlayerData> player) {

    }

    @Override
    public void playerDisconnected(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby, Player<MonopolyPlayerData> player) {

    }

    @Override
    public void playerReconnected(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby, Player<MonopolyPlayerData> player) {

    }

    @Override
    public void lobbyClosed(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby) {

    }
}
