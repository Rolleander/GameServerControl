package com.broll.networklib.server;

import com.broll.networklib.server.impl.ILobbyData;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ServerLobby;

import java.util.ArrayList;
import java.util.List;

public class LobbyServerListenerProxy<L extends ILobbyData, P extends ILobbyData>  implements ILobbyServerListener {

    private List<ILobbyServerListener<L,P>> listeners = new ArrayList<>();

    public LobbyServerListenerProxy(){

    }

    public void addListener(ILobbyServerListener<L, P> listener) {
        this.listeners.add(listener);
    }

    public void removeListener(ILobbyServerListener<L, P> listener) {
        this.listeners.add(listener);
    }
    @Override
    public void lobbyOpened(ServerLobby lobby) {
        listeners.forEach(it->it.lobbyOpened(lobby));
    }

    @Override
    public void playerJoined(ServerLobby lobby, Player player) {
        listeners.forEach(it->it.playerJoined(lobby, player));
    }

    @Override
    public void playerLeft(ServerLobby lobby, Player player) {
        listeners.forEach(it->it.playerLeft(lobby, player));
    }

    @Override
    public void playerDisconnected(ServerLobby lobby, Player player) {
        listeners.forEach(it->it.playerDisconnected(lobby, player));
    }

    @Override
    public void playerReconnected(ServerLobby lobby, Player player) {
        listeners.forEach(it->it.playerReconnected(lobby, player));
    }

    @Override
    public void lobbyClosed(ServerLobby lobby) {
        listeners.forEach(it->it.lobbyClosed(lobby));
    }
}