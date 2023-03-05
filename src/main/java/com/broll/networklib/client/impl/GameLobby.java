package com.broll.networklib.client.impl;

import com.broll.networklib.client.GameClient;
import com.broll.networklib.network.NetworkException;
import com.broll.networklib.network.nt.NT_ChatMessage;
import com.broll.networklib.network.nt.NT_LobbyLeave;

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

    private Map<Integer, LobbyPlayer> players = new HashMap<>();

    private IChatMessageListener chatMessageListener;

    private ILobbyUpdateListener lobbyUpdateListener;

    private Object settings;

    private GameClient client;

    private LobbyPlayer owner;

    GameLobby() {
        super();
    }


    void initClient(GameClient client) {
        this.client = client;
    }

    private void assureConnected() {
        if (client == null) {
            throw new NetworkException("Cannot send to unconnected lobby");
        }
    }

    public void sendChat(String message) {
        NT_ChatMessage chat = new NT_ChatMessage();
        chat.message = message;
        sendTCP(chat);
    }

    public void sendTCP(Object object) {
        assureConnected();
        client.sendTCP(object);
    }

    public void sendUDP(Object object) {
        assureConnected();
        client.sendUDP(object);
    }

    public void leave() {
        assureConnected();
        sendTCP(new NT_LobbyLeave());
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

    public void setChatMessageListener(IChatMessageListener chatMessageListener) {
        this.chatMessageListener = chatMessageListener;
    }

    public void setLobbyUpdateListener(ILobbyUpdateListener lobbyUpdateListener) {
        this.lobbyUpdateListener = lobbyUpdateListener;
    }

    ILobbyUpdateListener getLobbyUpdateListener() {
        return lobbyUpdateListener;
    }

    IChatMessageListener getChatMessageListener() {
        return chatMessageListener;
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

    void setOwner(LobbyPlayer owner) {
        this.owner = owner;
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

    public LobbyPlayer getOwner() {
        return owner;
    }

    void playerJoined(int playerId) {
        playerJoined = true;
        this.playerId = playerId;
        getPlayer(playerId).setMe(true);
    }

    public int getPlayerId() {
        return playerId;
    }

    Map<Integer, LobbyPlayer> getPlayerMap() {
        return players;
    }

}
