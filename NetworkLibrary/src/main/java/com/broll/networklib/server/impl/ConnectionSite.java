package com.broll.networklib.server.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.nt.NT_LobbyClosed;
import com.broll.networklib.network.nt.NT_LobbyCreate;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_LobbyJoin;
import com.broll.networklib.network.nt.NT_LobbyNoJoin;
import com.broll.networklib.network.nt.NT_LobbyKicked;
import com.broll.networklib.network.nt.NT_ServerInformation;
import com.broll.networklib.server.LobbyServerSite;
import com.broll.networklib.server.NetworkConnection;
import com.broll.networklib.server.PackageRestriction;
import com.broll.networklib.server.RestrictionType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionSite<L, P> extends LobbyServerSite<L, P> {

    private AtomicInteger playerIdCounter = new AtomicInteger();

    private Map<String, Player> playerRegister = new ConcurrentHashMap<>();

    private String serverName;

    public ConnectionSite(String name) {
        super();
        this.serverName = name;
    }

    @PackageRestriction(RestrictionType.NONE)
    @PackageReceiver
    public void receive(NT_ServerInformation serverInfo) {
        serverInfo.serverName = serverName;
        serverInfo.lobbies = lobbyHandler.getLobbies().stream().filter(ServerLobby::isVisible).map(ServerLobby::getLobbyInfo).toArray(NT_LobbyInformation[]::new);
        getConnection().sendTCP(serverInfo);
    }

    @PackageRestriction(RestrictionType.NOT_IN_LOBBY)
    @PackageReceiver
    public void joinLobby(NT_LobbyJoin join) {
        initPlayerAndJoinLobby(join.lobbyId, join.playerName, join.authenticationKey);
    }

    @PackageRestriction(RestrictionType.IN_LOBBY)
    @PackageReceiver
    public void switchLobby(NT_LobbyJoin join) {
        ServerLobby from = getLobby();
        if (from.isLocked()) {
            getConnection().sendTCP(new NT_LobbyNoJoin());
            return;
        }
        if (initPlayerAndJoinLobby(join.lobbyId, join.playerName, join.authenticationKey)) {
            //remove from previous lobby
            from.removePlayer(getPlayer());
        }
    }

    @PackageReceiver
    public void createLobby(NT_LobbyCreate create) {
        boolean reconnected = initPlayerConnection(create.playerName, create.authenticationKey);
        ServerLobby lobby = lobbyHandler.getLobbyCreationRequestHandler().createNewLobby(getPlayer(), create.lobbyName, create.settings);
        if(lobby!=null){
            joinLobby(lobby, reconnected, create.authenticationKey);
        }
        else{
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
                    playerRegister.remove(player.getAuthenticationKey());
                    lobby.removePlayer(player);
                }
            }
        }
    }

    private boolean initPlayerAndJoinLobby(int lobbyId, String playerName, String authenticationKey) {
        boolean reconnected = initPlayerConnection(playerName, authenticationKey);
        ServerLobby lobby = lobbyHandler.getLobby(lobbyId);
        if (lobby != null) {
           return joinLobby(lobby,reconnected, authenticationKey);
        }
        return false;
    }

    private boolean joinLobby(ServerLobby lobby, boolean reconnected, String authenticationKey){
        Player player = getPlayer();
        boolean successfulJoin = lobby.addPlayer(player);
        if (successfulJoin) {
            //put player in register
            playerRegister.put(authenticationKey, player);
            if (reconnected && player.getListener() != null) {
                player.getListener().reconnected(player);
            }
            getLobby().sendLobbyUpdate();
        }
        else{
            getConnection().sendTCP(new NT_LobbyNoJoin());
        }
        return successfulJoin;
    }

    private boolean initPlayerConnection(String playerName, String authenticationKey) {
        Player player = null;
        boolean reconnected = false;
        Player p = playerRegister.get(authenticationKey);
        if (!p.getConnection().isActive()) {
            //find existing player, for which the previous connection is inactive (prevent stealing when key is known)
            player = p;
            reconnected = true;
        }
        if (player == null) {
            //new player, key did not exist
            player = new Player(playerIdCounter.getAndIncrement(), authenticationKey, getConnection());
        }
        player.updateOnlineStatus(true);
        player.setName(playerName);
        getConnection().setPlayer(player);
        return reconnected;
    }

    public void closedLobby(ServerLobby lobby, List<Player<P>> players) {
        //remove players from register
        players.forEach(player ->      playerRegister.remove(player.getAuthenticationKey()));
        //send lobby closed to all player
        lobby.sendToAllTCP(new NT_LobbyClosed());
    }

    public void kickedPlayer(Player<P> player) {
        playerRegister.remove(player.getAuthenticationKey());
        player.sendTCP(new NT_LobbyKicked());
    }
}
