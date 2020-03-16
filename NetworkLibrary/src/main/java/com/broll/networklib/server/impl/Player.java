package com.broll.networklib.server.impl;


import com.broll.networklib.server.NetworkConnection;

public class Player {

    private String name;
    private final int id;
    private boolean online;
    private NetworkConnection connection;
    private ServerLobby serverLobby;

    public Player(int id, NetworkConnection connection) {
        this.id = id;
        this.connection = connection;
    }

    void joinLobby(ServerLobby lobby){
        this.serverLobby = serverLobby;
    }

    void leaveLobby(){
        serverLobby = null;
    }

    public boolean inLobby(){
        return serverLobby!=null;
    }

    public ServerLobby getServerLobby() {
        return serverLobby;
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

