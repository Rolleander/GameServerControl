package com.broll.networklib.server.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_LobbyJoin;
import com.broll.networklib.network.nt.NT_LobbyUnjoin;
import com.broll.networklib.network.nt.NT_ServerInformation;
import com.broll.networklib.server.LobbyServerSite;
import com.broll.networklib.server.NetworkConnection;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionSite<L,P> extends LobbyServerSite<L,P> {

    private AtomicInteger playerIdCounter = new AtomicInteger();

    private Map<String, Player> playerRegister = new ConcurrentHashMap<>();

    public ConnectionSite(LobbyHandler<L,P> lobbyHandler) {
        super(lobbyHandler);
    }

    @PackageReceiver
    public void receive(NT_ServerInformation serverInfo) {
        //connection requested lobby information, send information about all lobbies that are not locked
        serverInfo.lobbies = lobbyHandler.getLobbies().stream().filter(lobby-> !lobby.isLocked()).map(this::getLobbyInfo).toArray(NT_LobbyInformation[]::new);
        getConnection().sendTCP(serverInfo);
    }

    @PackageReceiver
    public void receive(NT_LobbyJoin join) {
        Player player = null;
        boolean reconnected = false;
        Player p = playerRegister.get(join.authenticationKey);
        if (!p.getConnection().isActive()) {
            //find existing player, for which the previous connection is inactive (prevent stealing when key is known)
            player = p;
            reconnected = true;
        }
        if (player == null) {
            //new player, key did not exist
            player = new Player(playerIdCounter.getAndIncrement(),join.authenticationKey, getConnection());
        }
        player.setName(join.name);
        getConnection().setPlayer(player);
        int id = join.lobbyId;
        ServerLobby lobby = lobbyHandler.getLobby(id);
        boolean successfulJoin = false;
        if (lobby != null) {
            successfulJoin = lobby.addPlayer(player);
            if(successfulJoin){
                //put player in register
                playerRegister.put(join.authenticationKey, player);
                if(reconnected && player.getListener()!=null){
                    player.getListener().reconnected(player);
                }
            }
        }
        if (!successfulJoin) {
            //send unjoin to client so he doesnt wait forever
            getConnection().sendTCP(new NT_LobbyUnjoin());
        }
    }

    @Override
    public void onDisconnect(NetworkConnection connection) {
        Player player = connection.getPlayer();
        if (player != null) {
            player.updateOnlineStatus(false);
            if(player.getListener()!=null){
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

    private NT_LobbyInformation getLobbyInfo(ServerLobby lobby){
        NT_LobbyInformation info = new NT_LobbyInformation();
        info.lobbyId = lobby.getId();
        info.lobbyName = lobby.getName();
        info.playerCount = lobby.getPlayerCount();
        info.playerLimit = lobby.getPlayerLimit();
        return info;
    }

    public void closedLobby(ServerLobby lobby, List<Player> players) {
        //remove players from register
        players.forEach(player-> playerRegister.remove(player.getAuthenticationKey()));
    }

}
