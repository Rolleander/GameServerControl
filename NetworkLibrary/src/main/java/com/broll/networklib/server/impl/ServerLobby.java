package com.broll.networklib.server.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ServerLobby {

    private int id;

    private List<Player> players = Collections.synchronizedList(new ArrayList<>());

    private boolean gameStarted;

    public ServerLobby(int id){
        this.id = id;
    }

    void addPlayer(Player player){
        players.add(player);
    }

    void removePlayer(Player player){
        players.remove(player);
    }

    public Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(players);
    }

    public int getId() {
        return id;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }
}
