package com.broll.networklib.client.impl;

import com.broll.networklib.client.GameClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GameLobby {

    private String serverIp;

    private String name;

    private int lobbyId;

    private int playerCount;

    private int playerLimit;

    private int playerId;

    private boolean playerJoined = false;

    private Map<Integer, LobbyPlayer> players;

    private ChatMessageListener chatMessageListener;

    private LobbyUpdateListener lobbyUpdateListener;

    private Object settings;

    GameLobby() {
        super();
    }

    public Optional<LobbyPlayer> getPlayer(String name) {
        return getPlayers().stream().filter(p -> Objects.equals(name, p.getName())).findFirst();
    }

    public LobbyPlayer getPlayer(int id) {
        return getPlayers().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public LobbyPlayer getMyPlayer() {
        return getPlayer(getPlayerId());
    }

    public void setChatMessageListener(ChatMessageListener chatMessageListener) {
        this.chatMessageListener = chatMessageListener;
    }

    public void setLobbyUpdateListener(LobbyUpdateListener lobbyUpdateListener) {
        this.lobbyUpdateListener = lobbyUpdateListener;
    }

    LobbyUpdateListener getLobbyUpdateListener() {
        return lobbyUpdateListener;
    }

    ChatMessageListener getChatMessageListener() {
        return chatMessageListener;
    }

    void setPlayers(Map<Integer, LobbyPlayer> players) {
        this.players = players;
    }

    public List<LobbyPlayer> getPlayers() {
        return new ArrayList<>(players.values());
    }

    void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    void setName(String name) {
        this.name = name;
    }

    void setLobbyId(int lobbyId) {
        this.lobbyId = lobbyId;
    }

    void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    void setSettings(Object settings) {
        this.settings = settings;
    }

    public boolean isPlayerJoined() {
        return playerJoined;
    }

    public boolean isFull() {
        return playerCount >= playerLimit;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getLobbyId() {
        return lobbyId;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public String getName() {
        return name;
    }

    public Object getSettings() {
        return settings;
    }

    void playerJoined(int playerId) {
        playerJoined = true;
        this.playerId = playerId;
        getPlayer(playerId).setMe(true);
    }

    public int getPlayerId() {
        return playerId;
    }
}
