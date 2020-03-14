package com.broll.networklib.player;


import com.broll.networklib.server.NetworkConnection;

public class Player {

    private String name;
    private final int id;
    private boolean online;
    private NetworkConnection connection;

    public Player(int id, NetworkConnection connection) {
        this.id = id;
        this.connection = connection;
    }

    public void updateOnlineStatus(boolean online) {
        this.online = online;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return online;
    }

    public NetworkConnection getConnection() {
        return connection;
    }
}

