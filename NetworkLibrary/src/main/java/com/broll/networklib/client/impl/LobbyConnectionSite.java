package com.broll.networklib.client.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.ClientSite;
import com.broll.networklib.network.NetworkRequestAttempt;
import com.broll.networklib.network.nt.NT_ChatMessage;
import com.broll.networklib.network.nt.NT_LobbyPlayerInfo;
import com.broll.networklib.network.nt.NT_LobbyUpdate;
import com.broll.networklib.network.nt.NT_LobbyUnjoin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LobbyConnectionSite extends ClientSite {

    private NetworkRequestAttempt<GameLobby> request;
    private GameLobby lobby;
    private Map<Integer, LobbyPlayer> players;

    public LobbyConnectionSite() {

    }

    public void tryJoinLobby(GameLobby lobby, NetworkRequestAttempt<GameLobby> request) {
        this.lobby = lobby;
        this.request = request;
        this.players = new HashMap<>();
    }

    @PackageReceiver
    public void receive(NT_LobbyUpdate lobbyUpdate) {
        //player could join lobby / lobby update
        if (lobby != null) {
            //update lobby info
            LobbyLookupSite.updateLobbyInfo(lobby, lobbyUpdate);
            updateLobbyPlayers(lobbyUpdate.players);
            if (!lobby.isPlayerJoined()) {
                joinLobby();
            }
            LobbyUpdateListener listener = lobby.getLobbyUpdateListener();
            if (listener != null) {
                listener.lobbyUpdated();
            }
        }
    }

    private void joinLobby() {
        players.clear();
        lobby.setPlayerJoined(true);
        lobby.setPlayers(players);
        //first update joins the player, so forward to request
        request.receive(lobby);
    }

    private void updateLobbyPlayers(NT_LobbyPlayerInfo[] playersInfo) {
        for (NT_LobbyPlayerInfo player : playersInfo) {
            int id = player.id;
            LobbyPlayer lobbyPlayer = players.get(id);
            if (lobbyPlayer == null) {
                playerJoined(player);
            } else {
                updatePlayer(lobbyPlayer, player);
            }
        }
        List<Integer> existingPlayers = Arrays.stream(playersInfo).map(p -> p.id).collect(Collectors.toList());
        players.entrySet().stream().filter(entry -> !existingPlayers.contains(entry.getKey())).forEach(entry -> playerLeft(entry.getValue(), entry.getKey()));
    }

    private void updatePlayer(LobbyPlayer player, NT_LobbyPlayerInfo info) {
        player.setName(info.name);
    }

    private void playerJoined(NT_LobbyPlayerInfo playerInfo) {
        LobbyPlayer player = new LobbyPlayer();
        updatePlayer(player, playerInfo);
        players.put(playerInfo.id, player);
        LobbyUpdateListener listener = lobby.getLobbyUpdateListener();
        if (listener != null) {
            listener.playerJoined(player);
        }
    }

    private void playerLeft(LobbyPlayer player, int id) {
        players.remove(id);
        LobbyUpdateListener listener = lobby.getLobbyUpdateListener();
        if (listener != null) {
            listener.playerLeft(player);
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
    public void receive(NT_LobbyUnjoin unjoin) {
        //player could not join lobby / was removed
        if (request != null) {
            lobby = null;
            players.clear();
            request.failure(unjoin.reason);
        }
    }
}
