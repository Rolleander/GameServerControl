package com.broll.networklib.server.impl;

import com.broll.networklib.network.nt.NT_LobbyPlayerInfo;
import com.broll.networklib.server.NetworkConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Player<P extends ILobbyData> {

    private String name;
    private final int id;
    private boolean online = true;
    private NetworkConnection connection;
    private ServerLobby serverLobby;

    private LobbyPlayer lobbyPlayer;
    private String authenticationKey;

    private List<IPlayerListener> listeners = new ArrayList<>();
    private P data;
    private Map<String, Object> sharedData = new HashMap<>();

    private boolean allowedToLeaveLockedLobby = false;

    public Player(int id, String authenticationKey, NetworkConnection connection) {
        this.id = id;
        this.authenticationKey = authenticationKey;
        this.connection = connection;
    }

    void removedFromLobby() {
        this.serverLobby = null;
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
        if(!online) return;
        connection.sendTCP(object);
    }

    public void sendUDP(Object object) {
        if(!online) return;
        connection.sendUDP(object);
    }

    public void addListener(IPlayerListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(IPlayerListener listener) {
        this.listeners.remove(listener);
    }


    List<IPlayerListener> getListeners() {
        return listeners;
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

    void updateOnlineStatus(boolean online) {
        this.online = online;
    }

    public void setAllowedToLeaveLockedLobby(boolean allowedToLeaveLockedLobby) {
        this.allowedToLeaveLockedLobby = allowedToLeaveLockedLobby;
    }

    public boolean isAllowedToLeaveLockedLobby() {
        return allowedToLeaveLockedLobby;
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

    private Object getSettings() {
        if (data == null) {
            return null;
        }
        return data.nt();
    }

    void setLobbyPlayer(LobbyPlayer lobbyPlayer) {
        this.lobbyPlayer = lobbyPlayer;
    }

    LobbyPlayer getLobbyPlayer() {
        return lobbyPlayer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player<?> player = (Player<?>) o;
        return id == player.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    NT_LobbyPlayerInfo nt() {
        NT_LobbyPlayerInfo info = new NT_LobbyPlayerInfo();
        info.id = getId();
        info.name = getName();
        info.settings = getSettings();
        return info;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }


}

