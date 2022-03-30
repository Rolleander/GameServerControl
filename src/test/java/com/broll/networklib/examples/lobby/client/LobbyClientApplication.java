package com.broll.networklib.examples.lobby.client;

import com.broll.networklib.client.LobbyGameClient;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.ILobbyUpdateListener;
import com.broll.networklib.client.impl.LobbyPlayer;
import com.broll.networklib.examples.lobby.LobbyNetworkRegistry;

public class LobbyClientApplication {

    public static void main(String[] args) {
        String playerName = "Peter";
        LobbyGameClient client = new LobbyGameClient(new LobbyNetworkRegistry());
        client.listLobbies("localhost").thenCompose(lobbies -> {
            System.out.println("# of listed lobbies: " + lobbies.getLobbies().size());
            GameLobby lobby = lobbies.getLobbies().get(0);
            return client.joinLobby(lobby, playerName);
        }).thenAccept(joinedLobby -> {
            System.out.println("joined lobby " + joinedLobby.getName());
            joinedLobby.setLobbyUpdateListener(new LobbyUpdateListener());
        }).exceptionally(exc -> {
            System.out.println("Failed: " + exc.getMessage());
            return null;
        }).thenRun(client::shutdown);
    }
}
