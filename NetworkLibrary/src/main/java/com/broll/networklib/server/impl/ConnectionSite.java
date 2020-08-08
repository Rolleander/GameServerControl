package com.broll.networklib.server.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.nt.NT_LobbyClosed;
import com.broll.networklib.network.nt.NT_LobbyCreate;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_LobbyJoin;
import com.broll.networklib.network.nt.NT_LobbyNoJoin;
import com.broll.networklib.network.nt.NT_LobbyKicked;
import com.broll.networklib.network.nt.NT_ReconnectCheck;
import com.broll.networklib.network.nt.NT_Reconnected;
import com.broll.networklib.network.nt.NT_ServerInformation;
import com.broll.networklib.server.LobbyServerSite;
import com.broll.networklib.server.NetworkConnection;
import com.broll.networklib.server.PackageRestriction;
import com.broll.networklib.server.RestrictionType;
import com.esotericsoftware.minlog.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionSite<L extends LobbySettings, P extends LobbySettings> extends LobbyServerSite<L, P> {

    private PlayerRegister playerRegister;
    private String serverName;

    public ConnectionSite(String name, PlayerRegister playerRegister) {
        super();
        this.serverName = name;
        this.playerRegister = playerRegister;
    }

    @PackageRestriction(RestrictionType.NONE)
    @PackageReceiver
    public void receive(NT_ServerInformation info) {
        NT_ServerInformation serverInfo = new NT_ServerInformation();
        serverInfo.serverName = serverName;
        serverInfo.lobbies = lobbyHandler.getLobbies().stream().filter(ServerLobby::isVisible).map(ServerLobby::getLobbyInfo).toArray(NT_LobbyInformation[]::new);
        getConnection().sendTCP(serverInfo);
    }

    @PackageRestriction(RestrictionType.NOT_IN_LOBBY)
    @PackageReceiver
    public void joinLobby(NT_LobbyJoin join) {
        initPlayerAndJoinLobby(join.lobbyId, join.playerName, join.authenticationKey);
    }

    @PackageRestriction(RestrictionType.NOT_IN_LOBBY)
    @PackageReceiver
    public void reconnectCheck(NT_ReconnectCheck check) {
        String key = check.authenticationKey;
        Player player = playerRegister.getPlayer(key);
        //player exists, has an inactive connection and is party of a lobby
        if (player != null && !player.getConnection().isActive() && player.getServerLobby() != null) {
            //reconnect player
            ServerLobby lobby = player.getServerLobby();
            NT_Reconnected reconnected = new NT_Reconnected();
            lobby.fillLobbyUpdate(reconnected);
            reconnected.playerName = player.getName();
            reconnectedPlayer(player);
            getConnection().sendTCP(reconnected);
        } else {
            //is a new player, cant be reconnected
            getConnection().sendTCP(new NT_LobbyNoJoin());
        }
    }

    @PackageRestriction(RestrictionType.IN_LOBBY)
    @PackageReceiver
    public void switchLobby(NT_LobbyJoin join) {
        ServerLobby from = getLobby();
        if (from.isLocked()) {
            getConnection().sendTCP(new NT_LobbyNoJoin());
            return;
        }
        if (lobbyHandler.getLobby(join.lobbyId) == from) {
            //already in the lobby, just init player
            boolean reconnect = initPlayerConnection(join.playerName, join.authenticationKey);
            if (reconnect) {
                reconnectedPlayer(getPlayer());
            }
            from.sendLobbyUpdate();
            return;
        }
        if (initPlayerAndJoinLobby(join.lobbyId, join.playerName, join.authenticationKey)) {
            //remove from previous lobby
            from.removePlayer(getPlayer());
            from.checkAutoClose();
        }
    }

    @PackageReceiver
    public void createLobby(NT_LobbyCreate create) {
        boolean reconnected = initPlayerConnection(create.playerName, create.authenticationKey);
        ServerLobby lobby = lobbyHandler.getLobbyCreationRequestHandler().createNewLobby(getPlayer(), create.lobbyName, create.settings);
        if (lobby != null) {
            joinLobby(lobby, reconnected);
        } else {
            //was not allowed to create lobby
            getConnection().sendTCP(new NT_LobbyNoJoin());
        }
    }

    @Override
    public void onDisconnect(NetworkConnection connection) {
        Player player = connection.getPlayer();
        if (player != null) {
            player.updateOnlineStatus(false);
            if (player.getListener() != null) {
                player.getListener().disconnected(player);
            }
            if (player.inLobby()) {
                ServerLobby lobby = player.getServerLobby();
                if (!lobby.isLocked()) {
                    //remove from lobby and register
                    playerRegister.unregister(player.getAuthenticationKey());
                    lobby.removePlayer(player);
                    lobby.checkAutoClose();
                }
            }
        }
    }

    private boolean initPlayerAndJoinLobby(int lobbyId, String playerName, String authenticationKey) {
        boolean reconnected = initPlayerConnection(playerName, authenticationKey);
        ServerLobby lobby = lobbyHandler.getLobby(lobbyId);
        if (lobby != null) {
            return joinLobby(lobby, reconnected);
        }
        return false;
    }

    private boolean joinLobby(ServerLobby lobby, boolean reconnected) {
        Player player = getPlayer();
        boolean successfulJoin = lobby.addPlayer(player);
        if (successfulJoin) {
            if (reconnected) {
                reconnectedPlayer(player);
            }
            lobby.sendLobbyUpdate();
        } else {
            getConnection().sendTCP(new NT_LobbyNoJoin());
        }
        return successfulJoin;
    }

    private void reconnectedPlayer(Player player) {
        getConnection().setPlayer(player);
        if (!player.isOnline()) {
            player.updateOnlineStatus(true);
            if (player.getListener() != null) {
                player.getListener().reconnected(player);
            }
        }
    }

    private boolean initPlayerConnection(String playerName, String authenticationKey) {
        boolean reconnected = false;
        Player player = playerRegister.getPlayer(authenticationKey);
        if (player == null) {
            //new player, key did not exist
            player = new Player(playerRegister.registerPlayerId(), authenticationKey, getConnection());
            //put player in register
            playerRegister.register(authenticationKey, player);
        } else if (!player.getConnection().isActive()) {
            //find existing player, for which the previous connection is inactive (prevent stealing when key is known)
            reconnected = true;
        }
        player.setName(playerName);
        getConnection().setPlayer(player);
        return reconnected;
    }

    public void closedLobby(ServerLobby lobby, List<Player<P>> players) {
        //remove players from register
        players.forEach(player -> playerRegister.unregister(player.getAuthenticationKey()));
        //send lobby closed to all player
        lobby.sendToAllTCP(new NT_LobbyClosed());
    }

    public void kickedPlayer(Player<P> player) {
        playerRegister.unregister(player.getAuthenticationKey());
        player.sendTCP(new NT_LobbyKicked());
    }
}
