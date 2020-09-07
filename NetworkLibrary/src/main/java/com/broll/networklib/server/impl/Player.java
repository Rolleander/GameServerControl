package com.broll.networklib.server.impl;

import com.broll.networklib.network.nt.NT_LobbyPlayerInfo;
import com.broll.networklib.server.NetworkConnection;

import java.util.HashMap;
import java.util.Map;

public class Player<P extends LobbySettings> {

    private String name;
    private final int id;
    private boolean online;
    private NetworkConnection connection;
    private ServerLobby serverLobby;
    private String authenticationKey;
    private PlayerListener listener;
    private P data;
    private Map<String, Object> sharedData = new HashMap<>();

    public Player(int id, String authenticationKey, NetworkConnection connection) {
        this.id = id;
        this.authenticationKey = authenticationKey;
        this.connection = connection;
    }

    void setConnection(NetworkConnection connection) {
        this.connection = connection;
    }

    public void setData(P data) {
        this.data = data;
    }

    public P getData() {
        return data;
    }

    public void sendTCP(Object object) {
        connection.sendTCP(object);
    }

    public void sendUDP(Object object) {
        connection.sendUDP(object);
    }

    public void setListener(PlayerListener listener) {
        this.listener = listener;
    }

    PlayerListener getListener() {
        return listener;
    }

    void setLobby(ServerLobby lobby) {
        this.serverLobby = lobby;
    }

    String getAuthenticationKey() {
        return authenticationKey;
    }

    public boolean inLobby() {
        return serverLobby != null;
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

    public Map<String, Object> getSharedData() {
        return sharedData;
    }

    public Object getSettings() {
        if (data == null) {return null;}
        return data.getSettings();
    }

     NT_LobbyPlayerInfo nt() {
        NT_LobbyPlayerInfo info = new NT_LobbyPlayerInfo();
        info.id = getId();
        info.name = getName();
        info.settings = getSettings();
        return info;
    }

}

