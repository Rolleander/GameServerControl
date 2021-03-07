package com.broll.networklib.client.impl;

import com.broll.networklib.client.GameClient;
import com.broll.networklib.client.auth.LastConnection;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_LobbyJoined;
import com.broll.networklib.network.nt.NT_LobbyPlayerInfo;
import com.broll.networklib.network.nt.NT_LobbyReconnected;
import com.broll.networklib.network.nt.NT_LobbyUpdate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class LobbyChange {

    public static GameLobby createdLobby(GameClient client, NT_LobbyJoined lobbyJoin, String ip) {
        GameLobby lobby = new GameLobby();
        lobby.setServerIp(ip);
        joinedLobby(client, lobby, lobbyJoin);
        return lobby;
    }

    public static GameLobby reconnectedLobby(NT_LobbyReconnected reconnected) {
        GameLobby lobby = new GameLobby();
        updateLobby(lobby, reconnected);
        lobby.playerJoined(reconnected.playerId);
        return lobby;
    }

    public static void joinedLobby(GameClient client, GameLobby lobby, NT_LobbyJoined lobbyJoin) {
        lobby.initClient(client);
        updateLobby(lobby, lobbyJoin);
        lobby.playerJoined(lobbyJoin.playerId);
    }

    public static void updateLobby(GameLobby lobby, NT_LobbyUpdate update) {
        synchronized (lobby) {
            updateLobbyInfo(lobby, update);
            updateLobbyPlayers(lobby, update.players);
            lobby.setOwner(lobby.getPlayers().stream().filter(it -> it.getId() == update.owner).findFirst().orElse(null));
        }
    }

    public static void updateLobbyInfo(GameLobby lobby, NT_LobbyInformation lobbyInfo) {
        lobby.setName(lobbyInfo.lobbyName);
        lobby.setLobbyId(lobbyInfo.lobbyId);
        lobby.setPlayerCount(lobbyInfo.playerCount);
        lobby.setPlayerLimit(lobbyInfo.playerLimit);
        lobby.setSettings(lobbyInfo.settings);
    }

    private static void updateLobbyPlayers(GameLobby lobby, NT_LobbyPlayerInfo[] playersInfo) {
        Map<Integer, LobbyPlayer> players = lobby.getPlayerMap();
        for (NT_LobbyPlayerInfo player : playersInfo) {
            int id = player.id;
            LobbyPlayer lobbyPlayer = players.get(id);
            if (lobbyPlayer == null) {
                playerJoined(lobby, player);
            } else {
                updatePlayer(lobbyPlayer, player);
            }
        }
        List<Integer> existingPlayers = Arrays.stream(playersInfo).map(p -> p.id).collect(Collectors.toList());
        players.entrySet().stream().filter(entry -> !existingPlayers.contains(entry.getKey())).collect(Collectors.toList()).stream().
                forEach(entry -> playerLeft(lobby, entry.getValue(), entry.getKey()));
    }

    private static void updatePlayer(LobbyPlayer player, NT_LobbyPlayerInfo info) {
        player.setName(info.name);
        player.setSettings(info.settings);
        player.setBot(info.bot);
    }

    private static void playerJoined(GameLobby lobby, NT_LobbyPlayerInfo playerInfo) {
        LobbyPlayer player = new LobbyPlayer(playerInfo.id, lobby);
        updatePlayer(player, playerInfo);
        lobby.getPlayerMap().put(playerInfo.id, player);
        LobbyUpdateListener listener = lobby.getLobbyUpdateListener();
        if (listener != null) {
            listener.playerJoined(player);
        }
    }

    private static void playerLeft(GameLobby lobby, LobbyPlayer player, int id) {
        lobby.getPlayerMap().remove(id);
        LobbyUpdateListener listener = lobby.getLobbyUpdateListener();
        if (listener != null) {
            listener.playerLeft(player);
        }
    }

}
