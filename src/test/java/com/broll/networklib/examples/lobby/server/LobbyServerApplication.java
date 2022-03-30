package com.broll.networklib.examples.lobby.server;

import com.broll.networklib.examples.lobby.LobbyNetworkRegistry;
import com.broll.networklib.examples.lobby.nt.NT_TokenType;
import com.broll.networklib.server.LobbyGameServer;
import com.broll.networklib.server.impl.ServerLobby;

public class LobbyServerApplication {

    public static void main(String[] args) {
        String serverName = "MonopolyServer";
        LobbyGameServer<MonopolyLobbyData, MonopolyPlayerData> server = new LobbyGameServer<>(serverName, new LobbyNetworkRegistry());
        server.open();
        String lobbyName = "TestLobby";
        ServerLobby<MonopolyLobbyData, MonopolyPlayerData> testLobby = server.getLobbyHandler().openLobby(lobbyName);
        MonopolyLobbyData lobbyData = new MonopolyLobbyData();
        lobbyData.setStartMoney(1500);
        testLobby.setData(lobbyData);
        testLobby.setPlayerLimit(NT_TokenType.values().length);
        testLobby.setListener(new MonopolyLobbyListener());
    }
}
