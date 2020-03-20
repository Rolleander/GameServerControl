package com.broll.networklib.server.impl;

import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_LobbyUpdate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerLobby<L, P> {

    private final static int NO_LIMIT = -1;

    private LobbyHandler<L,P> lobbyHandler;

    private int id;

    private int playerLimit = NO_LIMIT;

    private List<Player<P>> players = Collections.synchronizedList(new ArrayList<>());

    private boolean locked = false;

    private boolean hidden = false;

    private ServerLobbyListener listener;

    private String name;

    private L data;

    private Object settings;

    ServerLobby(LobbyHandler<L,P> lobbyHandler,String name, int id) {
        this.lobbyHandler =lobbyHandler;
        this.name = name;
        this.id = id;
    }

    public void close(){
        lobbyHandler.closeLobby(this);
    }

    public void kickPlayer(Player<P> player){
        lobbyHandler.kickPlayer(this,player);
    }

    public void transferPlayer(Player<P> player, ServerLobby<L,P> toLobby){
        lobbyHandler.transferPlayer(player, toLobby);
    }

    public void transferPlayers(ServerLobby<L,P> toLobby){
        lobbyHandler.transferPlayers(this,toLobby);
    }

    public ServerLobby<L,P> openCopy(){
        ServerLobby<L, P> lobby = lobbyHandler.openLobby(name, settings);
        lobby.hidden = hidden;
        lobby.playerLimit = playerLimit;
        lobby.data = data;
        return lobby;
    }

    public void sendToAllTCP(Object object) {
        getPlayers().forEach(player -> player.sendTCP(object));
    }

    public void sendToAllUDP(Object object) {
        getPlayers().forEach(player -> player.sendUDP(object));
    }

    public void setData(L data) {
        this.data = data;
    }

    public L getData() {
        return data;
    }

    public void setListener(ServerLobbyListener listener) {
        this.listener = listener;
    }

    synchronized boolean addPlayer(Player<P> player) {
        if (isFull() || locked) {
            return false;
        }
        players.add(player);
        player.setLobby(this);
        if (player.getListener() != null) {
            player.getListener().joinedLobby(player, this);
        }
        if (listener != null) {
            listener.playerJoined(this, player);
        }
        return true;
    }

    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    synchronized void close(LobbyCloseListener listener) {
        players.forEach(player -> player.setLobby(null));
        listener.closed(this, players);
        players.clear();
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isFull() {
        if (playerLimit == NO_LIMIT) {
            return false;
        }
        return players.size() < playerLimit;
    }

    void removePlayer(Player<P> player) {
        //only set lobby to null if player is from this lobby, to prevent unsetting lobby when transfering player
        if (player.getServerLobby() == this) {
            player.setLobby(null);
        }
        players.remove(player);
        if (player.getListener() != null) {
            player.getListener().leftLobby(player, this);
        }
        if (listener != null) {
            listener.playerLeft(this, player);
        }
    }

    public void sendLobbyUpdate() {
        NT_LobbyUpdate update = new NT_LobbyUpdate();
        fillLobbyInfo(update);
        sendToAllTCP(update);
    }

    private void fillLobbyInfo(NT_LobbyInformation info){
        info.lobbyId = getId();
        info.lobbyName = getName();
        info.playerCount = getPlayerCount();
        info.playerLimit = getPlayerLimit();
        info.settings = getSettings();
    }

     NT_LobbyInformation getLobbyInfo() {
        NT_LobbyInformation info = new NT_LobbyInformation();
        fillLobbyInfo(info);
        return info;
    }


    public int getPlayerCount() {
        return players.size();
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public Collection<Player<P>> getPlayers() {
        return Collections.unmodifiableCollection(players);
    }

    public int getId() {
        return id;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    boolean isVisible() {
        return !locked && !hidden;
    }

    public Object getSettings() {
        return settings;
    }

    public void setSettings(Object settings) {
        this.settings = settings;
    }
}
