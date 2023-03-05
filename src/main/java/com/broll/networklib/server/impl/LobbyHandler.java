package com.broll.networklib.server.impl;

import com.broll.networklib.network.nt.NT_LobbyKicked;
import com.broll.networklib.server.ILobbyServerListener;
import com.broll.networklib.server.LobbyServerSitesHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyHandler<L extends ILobbyData, P extends ILobbyData> {

    private final static Logger Log = LoggerFactory.getLogger(LobbyHandler.class);
    private AtomicInteger idCounter = new AtomicInteger();
    private Map<Integer, ServerLobby<L, P>> lobbies = new ConcurrentHashMap<>();
    private ILobbyCloseListener lobbyCloseListener;
    private ILobbyCreationRequest<L, P> lobbyCreationRequestHandler = (player, lobbyName, settings) -> this.openLobby(lobbyName);
    private PlayerRegister playerRegister;
    private LobbyServerSitesHandler sitesHandler;

    private ILobbyServerListener<L, P> serverListener;

    public LobbyHandler(ILobbyCloseListener lobbyCloseListener, PlayerRegister playerRegister, LobbyServerSitesHandler sitesHandler, ILobbyServerListener<L, P> serverListener) {
        this.lobbyCloseListener = lobbyCloseListener;
        this.playerRegister = playerRegister;
        this.sitesHandler = sitesHandler;
        this.serverListener = serverListener;
    }

    public void setLobbyCreationRequestHandler(ILobbyCreationRequest<L, P> lobbyCreationRequestHandler) {
        this.lobbyCreationRequestHandler = lobbyCreationRequestHandler;
    }

    ILobbyCreationRequest<L, P> getLobbyCreationRequestHandler() {
        return lobbyCreationRequestHandler;
    }

    public ServerLobby<L, P> openLobby(String name) {
        int id = idCounter.getAndIncrement();
        ServerLobby lobby = new ServerLobby(this, name, id, lobbyCloseListener);
        lobby.addListener(serverListener);
        lobbies.put(id, lobby);
        Log.info("Server opened lobby [" + id + "]: " + name);
        serverListener.lobbyOpened(lobby);
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
        lobbyCloseListener.kickedPlayer(player);
        lobby.sendLobbyUpdate();
        lobby.checkAutoClose();
    }

    public List<Player> transferPlayers(ServerLobby<L, P> from, ServerLobby<L, P> to) {
        List<Player> missedPlayers = new ArrayList<>();
        from.getActivePlayers().forEach(player -> {
            if (!transferPlayer(player, to)) {
                missedPlayers.add(player);
            }
        });
        return missedPlayers;
    }

    public boolean transferPlayer(Player player, ServerLobby<L, P> toLobby) {
        ServerLobby fromLobby = player.getServerLobby();
        if (fromLobby != null) {
            if ((fromLobby.isLocked() && !player.isAllowedToLeaveLockedLobby()) || toLobby.isLocked()) {
                return false;
            }
            if (toLobby.addPlayer(player)) {
                if (fromLobby != null) {
                    fromLobby.removePlayer(player);
                    fromLobby.sendLobbyUpdate();
                    fromLobby.checkAutoClose();
                    player.getListeners().forEach(it -> ((IPlayerListener)it).switchedLobby(player, fromLobby, toLobby));
                }
                return true;
            }
        }
        return false;
    }

    public Optional<BotPlayer<P>> createBot(ServerLobby<L, P> lobby, String name, P playerSettings) {
        BotConnection botConnection = new BotConnection();
        BotPlayer<P> bot = new BotPlayer<>(playerRegister.registerPlayerId(), botConnection);
        bot.setName(name);
        bot.setData(playerSettings);
        botConnection.setPlayer(bot);
        botConnection.init(new BotEndpoint(sitesHandler, botConnection));
        if (lobby.addPlayer(bot)) {
            playerRegister.register(bot.getAuthenticationKey(), bot);
            lobby.sendLobbyJoinUpdate(bot);
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
