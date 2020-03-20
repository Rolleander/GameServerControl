package com.broll.networklib.server.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyHandler<L, P> {

    private AtomicInteger idCounter;
    private Map<Integer, ServerLobby<L,P>> lobbies = new ConcurrentHashMap<>();
    private LobbyCloseListener listener;
    private ILobbyCreationRequest<L,P> lobbyCreationRequestHandler = (player, lobbyName, settings)->this.openLobby(lobbyName, settings);

    public LobbyHandler(LobbyCloseListener listener) {
        this.listener = listener;
    }

    public void setLobbyCreationRequestHandler(ILobbyCreationRequest<L, P> lobbyCreationRequestHandler) {
        this.lobbyCreationRequestHandler = lobbyCreationRequestHandler;
    }

    ILobbyCreationRequest<L, P> getLobbyCreationRequestHandler() {
        return lobbyCreationRequestHandler;
    }

    public ServerLobby<L,P> openLobby(String name, Object settings) {
        int id = idCounter.getAndIncrement();
        ServerLobby lobby = new ServerLobby(this, name, id);
        lobby.setSettings(settings);
        lobbies.put(id, lobby);
        return lobby;
    }

    public void closeAllLobbies() {
        do {
            Iterator<ServerLobby<L, P>> iterator = lobbies.values().iterator();
            while (iterator.hasNext()) {
                closeLobby(iterator.next());
                iterator.remove();
            }
        } while (!lobbies.isEmpty());
    }

    public void closeLobby(ServerLobby<L, P> lobby) {
        lobby.close(listener);
        lobbies.remove(lobby.getId());
    }

    public void kickPlayer(ServerLobby<L, P> lobby, Player<P> player){
        lobby.removePlayer(player);
        listener.kickedPlayer(player);
        if(lobby.getPlayerCount()==0){
            closeLobby(lobby);
        }
    }

    public List<Player> transferPlayers(ServerLobby<L, P> from, ServerLobby<L, P> to) {
        List<Player> missedPlayers = new ArrayList<>();
        from.getPlayers().forEach(player -> {
            if (!transferPlayer(player, to)) {
                missedPlayers.add(player);
            }
        });
        return missedPlayers;
    }

    public boolean transferPlayer(Player player, ServerLobby<L, P> toLobby) {
        ServerLobby fromLobby = player.getServerLobby();
        if (toLobby.addPlayer(player)) {
            if (fromLobby != null) {
                fromLobby.removePlayer(player);
                if(fromLobby.getPlayerCount()==0){
                    closeLobby(fromLobby);
                }
            }
            if(player.getListener()!=null){
                player.getListener().switchedLobby(player, fromLobby, toLobby);
            }
            return true;
        }
        return false;
    }

    public ServerLobby<L, P> getLobby(int id) {
        return lobbies.get(id);
    }

    public Collection<ServerLobby<L, P>> getLobbies() {
        return Collections.unmodifiableCollection(lobbies.values());
    }

}
