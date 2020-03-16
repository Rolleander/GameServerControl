package com.broll.networklib.server.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyHandler {

    private AtomicInteger idCounter;
    private Map<Integer, ServerLobby> lobbies = new ConcurrentHashMap<>();

    public LobbyHandler(){

    }

    public ServerLobby openLobby(){
        int id = idCounter.getAndIncrement();
        ServerLobby lobby = new ServerLobby(id);
        lobbies.put(id, lobby);
        return lobby;
    }

    public void closeLobby(ServerLobby lobby){
        lobbies.remove(lobby.getId());
    }

    public ServerLobby getLobby(int id){
        return lobbies.get(id);
    }

    public Collection<ServerLobby> getLobbies(){
        return Collections.unmodifiableCollection(lobbies.values());
    }

}
