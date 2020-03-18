package com.broll.networklib.server.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerLobby<L, P> {

    private final static int NO_LIMIT = -1;

    private int id;

    private int playerLimit = NO_LIMIT;

    private List<Player<P>> players = Collections.synchronizedList(new ArrayList<>());

    private boolean locked = false;

    private ServerLobbyListener listener;

    private String name;

    private L data;

    ServerLobby(String name, int id) {
        this.id = id;
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

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
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
}
