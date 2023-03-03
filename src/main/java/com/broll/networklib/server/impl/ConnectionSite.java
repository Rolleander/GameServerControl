package com.broll.networklib.server.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.nt.NT_LobbyClosed;
import com.broll.networklib.network.nt.NT_LobbyCreate;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_LobbyJoin;
import com.broll.networklib.network.nt.NT_LobbyNoJoin;
import com.broll.networklib.network.nt.NT_LobbyKicked;
import com.broll.networklib.network.nt.NT_LobbyReconnected;
import com.broll.networklib.network.nt.NT_ReconnectCheck;
import com.broll.networklib.network.nt.NT_ServerInformation;
import com.broll.networklib.server.LobbyServerSite;
import com.broll.networklib.server.NetworkConnection;
import com.broll.networklib.server.ConnectionRestriction;
import com.broll.networklib.server.RestrictionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class ConnectionSite<L extends ILobbyData, P extends ILobbyData> extends LobbyServerSite<L, P> {

    private final static Logger Log = LoggerFactory.getLogger(ConnectionSite.class);
    private PlayerRegister playerRegister;
    private String serverName;

    private String version;

    public ConnectionSite() {
        //required for cloning
    }

    public ConnectionSite(String name, PlayerRegister playerRegister) {
        super();
        this.serverName = name;
        this.playerRegister = playerRegister;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @ConnectionRestriction(RestrictionType.NONE)
    @PackageReceiver
    public void receive(NT_ServerInformation info) {
        NT_ServerInformation serverInfo = new NT_ServerInformation();
        serverInfo.serverName = serverName;
        serverInfo.lobbies = lobbyHandler.getLobbies().stream().filter(ServerLobby::isVisible).map(ServerLobby::getLobbyInfo).toArray(NT_LobbyInformation[]::new);
        getConnection().sendTCP(serverInfo);
    }

    @ConnectionRestriction(RestrictionType.NOT_IN_LOBBY)
    @PackageReceiver
    public void joinLobby(NT_LobbyJoin join) {
        if(checkJoiningClientVersion(join.version)){
            initPlayerAndJoinLobby(join.lobbyId, join.playerName, join.authenticationKey);
        }
    }

    @ConnectionRestriction(RestrictionType.NOT_IN_LOBBY)
    @PackageReceiver
    public void reconnectCheck(NT_ReconnectCheck check) {
        Log.info("check reconnect");
        String key = check.authenticationKey;
        Player player = playerRegister.getPlayer(key);
        //player exists, has an inactive connection and is party of a lobby
        if (player != null && !player.getConnection().isActive() && player.getServerLobby() != null) {
            //reconnect player
            ServerLobby lobby = player.getServerLobby();
            NT_LobbyReconnected reconnected = new NT_LobbyReconnected();
            lobby.fillLobbyUpdate(reconnected);
            reconnected.playerId = player.getId();
            reconnectedPlayer(player);
            getConnection().sendTCP(reconnected);
            lobby.playerChangedConnectionStatus(player, true);
        } else {
            //is a new player, cant be reconnected
            Log.warn("Reconnect check failed: player is new and cannot be reconnected!");
            getConnection().sendTCP(new NT_LobbyNoJoin());
        }
    }

    @ConnectionRestriction(RestrictionType.IN_LOBBY)
    @PackageReceiver
    public void switchLobby(NT_LobbyJoin join) {
        ServerLobby from = getLobby();
        if (from.isLocked() && !getPlayer().isAllowedToLeaveLockedLobby()) {
            Log.warn("Player "+getPlayer()+" is not allowed to switch lobby!");
            getConnection().sendTCP(new NT_LobbyNoJoin());
            return;
        }
        if (lobbyHandler.getLobby(join.lobbyId) == from) {
            //already in the lobby, just init player
            boolean reconnect = initPlayerConnection(join.playerName, join.authenticationKey);
            if (reconnect) {
                reconnectedPlayer(getPlayer());
            }
            from.sendLobbyJoinUpdate(getPlayer());
            return;
        }
        if (initPlayerAndJoinLobby(join.lobbyId, join.playerName, join.authenticationKey)) {
            //remove from previous lobby
            from.removePlayer(getPlayer());
            from.checkAutoClose();
            if (!from.isClosed()) {
                from.sendLobbyUpdate();
            }
        }
    }

    @ConnectionRestriction(RestrictionType.NOT_IN_LOBBY)
    @PackageReceiver
    public void createLobby(NT_LobbyCreate create) {
        if(!checkJoiningClientVersion(create.version)){
            return;
        }
        boolean reconnected = initPlayerConnection(create.playerName, create.authenticationKey);
        ServerLobby lobby = lobbyHandler.getLobbyCreationRequestHandler().createNewLobby(getPlayer(), create.lobbyName, create.settings);
        if (lobby != null) {
            joinLobby(lobby, reconnected);
        } else {
            Log.warn("Player "+getPlayer()+" was not allowed to create lobby!");
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
                if (lobby.isLocked() && !player.isAllowedToLeaveLockedLobby()) {
                    //notify lobby player disconnected
                    lobby.playerChangedConnectionStatus(player, false);
                } else {
                    //remove from lobby and register
                    playerRegister.unregister(player.getAuthenticationKey());
                    lobby.removePlayer(player);
                    lobby.checkAutoClose();
                    if (!lobby.isClosed()) {
                        lobby.sendLobbyUpdate();
                    }
                }
            }
        }
    }

    private boolean checkJoiningClientVersion(String clientVersion){
        if (!Objects.equals(this.version, clientVersion)) {
            Log.warn("Player "+getPlayer()+" version does not match server!");
            NT_LobbyNoJoin nt = new NT_LobbyNoJoin();
            nt.reason = "Version mismatch with server: " + version;
            getConnection().sendTCP(nt);
            return false;
        }
        return true;
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
            lobby.sendLobbyJoinUpdate(player);
        } else {
            getConnection().sendTCP(new NT_LobbyNoJoin());
        }
        return successfulJoin;
    }

    private void reconnectedPlayer(Player player) {
        player.setConnection(getConnection());
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
        if (getPlayer() != null) {
            //a player is already initialized for this connection, nothing to do
            return false;
        }
        Player player = playerRegister.getPlayer(authenticationKey);
        if (player == null) {
            //new player, key did not exist
            player = new Player(playerRegister.registerPlayerId(), authenticationKey, getConnection());
            //put player in register
            playerRegister.register(authenticationKey, player);
        } else {
            if (player.getConnection().isActive()) {
                //someone else trying to steal the player session with its key?
                //do not allow login
                getConnection().sendTCP(new NT_LobbyNoJoin());
                throw new RuntimeException(authenticationKey + " already belongs to " + player + " who is still active, do not allow login for " + playerName);
            } else {
                reconnected = true;
            }
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
