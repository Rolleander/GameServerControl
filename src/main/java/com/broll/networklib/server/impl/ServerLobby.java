package com.broll.networklib.server.impl;

import com.broll.networklib.network.nt.NT_ChatMessage;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_LobbyJoined;
import com.broll.networklib.network.nt.NT_LobbyLock;
import com.broll.networklib.network.nt.NT_LobbyPlayerInfo;
import com.broll.networklib.network.nt.NT_LobbyUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServerLobby<L extends ILobbyData, P extends ILobbyData> {

    private final static Logger Log = LoggerFactory.getLogger(ServerLobby.class);

    public final static int NO_PLAYER_LIMIT = -1;

    private LobbyHandler<L, P> lobbyHandler;

    private int id;

    private Player<P> owner;

    private int playerLimit = NO_PLAYER_LIMIT;

    private List<Player<P>> activePlayers = Collections.synchronizedList(new ArrayList<>());
    private List<LobbyPlayer<P>> lobbyPlayers = Collections.synchronizedList(new ArrayList<>());

    private boolean locked = false;

    private boolean hidden = false;

    private boolean closed = false;

    private boolean autoClose = true;

    private List<IServerLobbyListener> listeners = new ArrayList<>();

    private ILobbyCloseListener<L, P> closeListener;

    private String name;

    private L data;

    private Map<String, Object> sharedData = new HashMap<>();

    ServerLobby(LobbyHandler<L, P> lobbyHandler, String name, int id, ILobbyCloseListener<L, P> closeListener) {
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
        getActivePlayers().forEach(player -> player.sendTCP(object));
    }

    public void sendToAllUDP(Object object) {
        getActivePlayers().forEach(player -> player.sendUDP(object));
    }

    public void setData(L data) {
        this.data = data;
    }

    public L getData() {
        return data;
    }


    public void addListener(IServerLobbyListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(IServerLobbyListener listener) {
        this.listeners.remove(listener);
    }

    synchronized boolean addPlayer(Player<P> player) {
        if (isFull() || locked || closed) {
            Log.warn("Lobby update [" + id + "] " + name + " | Can not add player " + player.getName() + ", lobby is full or locked!");
            return false;
        }
        if (activePlayers.contains(player)) {
            Log.warn("Lobby update [" + id + "] " + name + " | Player " + player.getName() + " has already joined lobby!");
            return false;
        }
        player.setAllowedToLeaveLockedLobby(false);
        activePlayers.add(player);
        lobbyPlayers.add(new LobbyPlayer<>(player));
        player.setLobby(this);
        assignOwner();
        Log.info("Lobby update [" + id + "] " + name + " | Player " + player.getName() + " joined!");
        player.getListeners().forEach(it -> it.joinedLobby(player, this));
        listeners.forEach(it -> it.playerJoined(this, player));
        return true;
    }

    private void assignOwner() {
        if (owner == null) {
            if (!activePlayers.isEmpty()) {
                //find next non bot and make him owner
                getActivePlayers().stream().filter(it -> !(it instanceof BotPlayer)).findFirst().ifPresent(player -> {
                    this.owner = player;
                });
            }
        } else {
            if (!activePlayers.contains(owner)) {
                //owner left, pick new random one
                owner = null;
                assignOwner();
            }
        }
    }

    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    public Collection<BotPlayer<P>> getBots() {
        return getActivePlayers().stream().filter(player -> player instanceof BotPlayer).map(player -> (BotPlayer<P>) player).collect(Collectors.toList());
    }

    public Collection<Player<P>> getRealPlayers() {
        return getActivePlayers().stream().filter(player -> !(player instanceof BotPlayer)).collect(Collectors.toList());
    }

    public Optional<BotPlayer<P>> createBot(String name, P playerSettings) {
        return lobbyHandler.createBot(this, name, playerSettings);
    }

    private boolean hasRealPlayers() {
        return getActivePlayers().stream().filter(player -> !(player instanceof BotPlayer)).findAny().isPresent();
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
        activePlayers.forEach(player -> player.setLobby(null));
        closeListener.closed(this, activePlayers);
        activePlayers.clear();
        lobbyPlayers.clear();
        listeners.forEach(it -> it.lobbyClosed(this));
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public synchronized void lock() {
        if (!this.locked) {
            updateLock(true);
        }
    }

    public synchronized void unlock() {
        if (this.locked) {
            updateLock(false);
        }
    }

    private void updateLock(boolean lock) {
        this.locked = lock;
        NT_LobbyLock nt = new NT_LobbyLock();
        nt.locked = lock;
        sendToAllTCP(nt);
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isFull() {
        if (playerLimit == NO_PLAYER_LIMIT) {
            return false;
        }
        return activePlayers.size() >= playerLimit;
    }

    void playerChangedConnectionStatus(Player<P> player, boolean connected) {
        if (connected) {
            Log.info("Player " + player + " reconnected to lobby " + this);
            listeners.forEach(it -> it.playerReconnected(this, player));
        } else {
            Log.info("Player " + player + " disconnected from lobby " + this);
            listeners.forEach(it -> it.playerDisconnected(this, player));
        }
    }

    synchronized void removePlayer(Player<P> player) {
        if (!activePlayers.contains(player)) {
            Log.warn("Lobby update [" + id + "] " + name + " | Player " + player.getName() + " cannot remove player from lobby that is not part of its players!");
            return;
        }
        //only set lobby to null if player is from this lobby, to prevent unsetting lobby when transfering player
        if (player.getServerLobby() == this) {
            player.removedFromLobby();
        }
        activePlayers.remove(player);
        if (locked) {
            player.getLobbyPlayer().leftLobby();
        } else {
            lobbyPlayers.remove(player.getLobbyPlayer());
        }
        assignOwner();
        Log.info("Lobby update [" + id + "] " + name + " | Player " + player.getName() + " left.");
        player.getListeners().forEach(it -> it.leftLobby(player, this));
        listeners.forEach(it -> it.playerLeft(this, player));
    }

    public void chat(String from, String message) {
        NT_ChatMessage chat = new NT_ChatMessage();
        chat.from = from;
        chat.message = message;
        sendToAllTCP(chat);
    }

    public void sendLobbyUpdate() {
        NT_LobbyUpdate update = new NT_LobbyUpdate();
        fillLobbyUpdate(update);
        sendToAllTCP(update);
    }

    void sendLobbyJoinUpdate(Player<P> joinedPlayer) {
        NT_LobbyUpdate update = new NT_LobbyUpdate();
        NT_LobbyJoined joined = new NT_LobbyJoined();
        joined.playerId = joinedPlayer.getId();
        fillLobbyUpdate(update);
        fillLobbyUpdate(joined);
        getActivePlayers().forEach(p -> {
            if (p == joinedPlayer) {
                p.sendTCP(joined);
            } else {
                p.sendTCP(update);
            }
        });
    }

    void fillLobbyUpdate(NT_LobbyUpdate update) {
        fillLobbyInfo(update);
        update.players = getActivePlayers().stream().map(Player::nt).toArray(NT_LobbyPlayerInfo[]::new);
        if (owner != null) {
            update.owner = owner.getId();
        }
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
        return activePlayers.size();
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public Collection<Player<P>> getActivePlayers() {
        return Collections.unmodifiableCollection(activePlayers);
    }

    public Collection<LobbyPlayer<P>> getPlayers() {
        return Collections.unmodifiableCollection(lobbyPlayers);
    }

    public List<P> getPlayersData() {
        return getPlayers().stream().map(LobbyPlayer::getData).collect(Collectors.toList());
    }

    public Player<P> getPlayer(int id) {
        return activePlayers.stream().filter(player -> player.getId() == id).findFirst().orElse(null);
    }

    public LobbyHandler<L, P> getLobbyHandler() {
        return lobbyHandler;
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

    private Object getSettings() {
        if (data == null) {
            return null;
        }
        return data.nt();
    }

    public Player<P> getOwner() {
        return owner;
    }

    public Map<String, Object> getSharedData() {
        return sharedData;
    }
}
