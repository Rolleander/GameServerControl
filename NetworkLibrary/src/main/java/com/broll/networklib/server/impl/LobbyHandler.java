package com.broll.networklib.server.impl;

import com.broll.networklib.network.nt.NT_LobbyKicked;
import com.broll.networklib.server.LobbyServerSitesHandler;
import com.esotericsoftware.minlog.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyHandler<L extends LobbySettings, P extends LobbySettings> {

    private AtomicInteger idCounter = new AtomicInteger();
    private Map<Integer, ServerLobby<L, P>> lobbies = new ConcurrentHashMap<>();
    private LobbyCloseListener listener;
    private ILobbyCreationRequest<L, P> lobbyCreationRequestHandler = (player, lobbyName, settings) -> this.openLobby(lobbyName);
    private PlayerRegister playerRegister;
    private LobbyServerSitesHandler sitesHandler;

    public LobbyHandler(LobbyCloseListener listener, PlayerRegister playerRegister, LobbyServerSitesHandler sitesHandler ) {
        this.listener = listener;
        this.playerRegister = playerRegister;
        this.sitesHandler = sitesHandler;
    }

    public void setLobbyCreationRequestHandler(ILobbyCreationRequest<L, P> lobbyCreationRequestHandler) {
        this.lobbyCreationRequestHandler = lobbyCreationRequestHandler;
    }

    ILobbyCreationRequest<L, P> getLobbyCreationRequestHandler() {
        return lobbyCreationRequestHandler;
    }

    public ServerLobby<L, P> openLobby(String name) {
        int id = idCounter.getAndIncrement();
        ServerLobby lobby = new ServerLobby(this, name, id, listener);
        lobbies.put(id, lobby);
        Log.info("Server opened lobby [" + id + "]: " + name);
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
        lobby.remove();
        lobbies.remove(lobby.getId());
        //send lobby close for remaining players
        NT_LobbyKicked lobbyKicked = new NT_LobbyKicked();
        lobbyKicked.reason = NT_LobbyKicked.REASON_LOBBY_CLOSED;
        lobby.sendToAllTCP(lobbyKicked);
        Log.info("Server closed lobby [" + lobby.getId() + "]: " + lobby.getName());
    }

    public void kickPlayer(ServerLobby<L, P> lobby, Player<P> player) {
        lobby.removePlayer(player);
        listener.kickedPlayer(player);
        lobby.checkAutoClose();
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
        if (fromLobby != null) {
            if (fromLobby.isLocked() || toLobby.isLocked()) {
                return false;
            }
            if (toLobby.addPlayer(player)) {
                if (fromLobby != null) {
                    fromLobby.removePlayer(player);
                    fromLobby.checkAutoClose();
                }
                if (player.getListener() != null) {
                    player.getListener().switchedLobby(player, fromLobby, toLobby);
                }
                return true;
            }
        }
        return false;
    }

    public Optional<BotPlayer<P>> createBot(ServerLobby<L, P> lobby, String name, P playerSettings) {
        BotConnection botConnection = new BotConnection();
        botConnection.init(new BotEndpoint(sitesHandler, botConnection));
        BotPlayer<P> bot = new BotPlayer<>(playerRegister.registerPlayerId(), botConnection);
        bot.setName(name);
        botConnection.setPlayer(bot);
        bot.setData(playerSettings);
        if (lobby.addPlayer(bot)) {
            playerRegister.register(bot.getAuthenticationKey(), bot);
            return Optional.of(bot);
        }
        return Optional.empty();
    }

    public ServerLobby<L, P> getLobby(int id) {
        return lobbies.get(id);
    }

    public Collection<ServerLobby<L, P>> getLobbies() {
        return Collections.unmodifiableCollection(lobbies.values());
    }

}
