package com.broll.networklib.server.impl;

import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_LobbyPlayerInfo;
import com.broll.networklib.network.nt.NT_LobbyUpdate;
import com.esotericsoftware.minlog.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerLobby<L extends LobbySettings, P extends LobbySettings> {

    private final static int NO_LIMIT = -1;

    private LobbyHandler<L, P> lobbyHandler;

    private int id;

    private int playerLimit = NO_LIMIT;

    private List<Player<P>> players = Collections.synchronizedList(new ArrayList<>());

    private boolean locked = false;

    private boolean hidden = false;

    private boolean closed = false;

    private boolean autoClose = true;

    private ServerLobbyListener listener;

    private LobbyCloseListener<L, P> closeListener;

    private String name;

    private L data;

    ServerLobby(LobbyHandler<L, P> lobbyHandler, String name, int id, LobbyCloseListener<L, P> closeListener) {
        this.lobbyHandler = lobbyHandler;
        this.name = name;
        this.id = id;
        this.closeListener = closeListener;
    }

    public void close() {
        lobbyHandler.closeLobby(this);
    }

    public void kickPlayer(Player<P> player) {
        lobbyHandler.kickPlayer(this, player);
    }

    public boolean transferPlayer(Player<P> player, ServerLobby<L, P> toLobby) {
        return lobbyHandler.transferPlayer(player, toLobby);
    }

    public List<Player> transferPlayers(ServerLobby<L, P> toLobby) {
        return lobbyHandler.transferPlayers(this, toLobby);
    }

    public ServerLobby<L, P> openCopy() {
        ServerLobby<L, P> lobby = lobbyHandler.openLobby(name);
        lobby.hidden = hidden;
        lobby.playerLimit = playerLimit;
        lobby.data = data;
        return lobby;
    }

    public void sendToAllTCP(Object object) {
        getPlayers().forEach(player -> player.sendTCP(object));
    }

    public void sendToAllUDP(Object object) {
        getPlayers().forEach(player -> player.sendUDP(object));
    }

    public void setData(L data) {
        this.data = data;
    }

    public L getData() {
        return data;
    }

    public void setListener(ServerLobbyListener listener) {
        this.listener = listener;
    }

    public synchronized void synchronizedAccess(Runnable runnable) {
        runnable.run();
    }

    synchronized boolean addPlayer(Player<P> player) {
        if (isFull() || locked || closed) {
            Log.warn("Lobby update [" + id + "] " + name + " | Can not add player " + player.getName() + ", lobby is full or locked!");
            return false;
        }
        if (players.contains(player)) {
            Log.warn("Lobby update [" + id + "] " + name + " | Player " + player.getName() + " has already joined lobby!");
            return false;
        }
        players.add(player);
        player.setLobby(this);
        Log.info("Lobby update [" + id + "] " + name + " | Player " + player.getName() + " joined!");
        if (player.getListener() != null) {
            player.getListener().joinedLobby(player, this);
        }
        if (listener != null) {
            listener.playerJoined(this, player);
        }
        return true;
    }

    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    public Collection<BotPlayer<P>> getBots() {
        return getPlayers().stream().filter(player -> player instanceof BotPlayer).map(player -> (BotPlayer<P>) player).collect(Collectors.toList());
    }

    public Optional<BotPlayer<P>> createBot(String name, P playerSettings) {
        return lobbyHandler.createBot(this, name, playerSettings);
    }

    private boolean hasRealPlayers() {
        return getPlayers().stream().filter(player -> !(player instanceof BotPlayer)).findAny().isPresent();
    }

    void checkAutoClose() {
        if (autoClose && !closed && !hasRealPlayers()) {
            close();
        }
    }

    synchronized void remove() {
        closed = true;
        locked = true;
        hidden = true;
        players.forEach(player -> player.setLobby(null));
        closeListener.closed(this, players);
        players.clear();
        if (listener != null) {
            listener.lobbyClosed(this);
        }
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isFull() {
        if (playerLimit == NO_LIMIT) {
            return false;
        }
        return players.size() < playerLimit;
    }

    void removePlayer(Player<P> player) {
        //only set lobby to null if player is from this lobby, to prevent unsetting lobby when transfering player
        if (player.getServerLobby() == this) {
            player.setLobby(null);
        }
        players.remove(player);
        Log.info("Lobby update [" + id + "] " + name + " | Player " + player.getName() + " left.");
        if (player.getListener() != null) {
            player.getListener().leftLobby(player, this);
        }
        if (listener != null) {
            listener.playerLeft(this, player);
        }
    }

    public void sendLobbyUpdate() {
        NT_LobbyUpdate update = new NT_LobbyUpdate();
        fillLobbyUpdate(update);
        sendToAllTCP(update);
    }

    void fillLobbyUpdate(NT_LobbyUpdate update) {
        fillLobbyInfo(update);
        update.players = getPlayers().stream().map(this::createPlayerInfo).toArray(NT_LobbyPlayerInfo[]::new);
    }

    private NT_LobbyPlayerInfo createPlayerInfo(Player player) {
        NT_LobbyPlayerInfo info = new NT_LobbyPlayerInfo();
        info.id = player.getId();
        info.name = player.getName();
        info.settings = player.getSettings();
        return info;
    }

    void fillLobbyInfo(NT_LobbyInformation info) {
        info.lobbyId = getId();
        info.lobbyName = getName();
        info.playerCount = getPlayerCount();
        info.playerLimit = getPlayerLimit();
        info.settings = getSettings();
    }

    NT_LobbyInformation getLobbyInfo() {
        NT_LobbyInformation info = new NT_LobbyInformation();
        fillLobbyInfo(info);
        return info;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public Collection<Player<P>> getPlayers() {
        return Collections.unmodifiableCollection(players);
    }

    public Player<P> getPlayer(int id) {
        return players.stream().filter(player -> player.getId() == id).findFirst().orElse(null);
    }

    public Stream<P> streamData() {
        return getPlayers().stream().map(Player::getData);
    }

    public int getId() {
        return id;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    boolean isVisible() {
        return !locked && !hidden;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public Object getSettings() {
        if (data == null) {
            return null;
        }
        return data.getSettings();
    }

}
