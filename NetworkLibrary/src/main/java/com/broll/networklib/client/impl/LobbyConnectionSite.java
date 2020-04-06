package com.broll.networklib.client.impl;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.ClientAuthenticationKey;
import com.broll.networklib.client.ClientSite;
import com.broll.networklib.network.INetworkRequestAttempt;
import com.broll.networklib.network.nt.NT_ChatMessage;
import com.broll.networklib.network.nt.NT_LobbyClosed;
import com.broll.networklib.network.nt.NT_LobbyCreate;
import com.broll.networklib.network.nt.NT_LobbyJoin;
import com.broll.networklib.network.nt.NT_LobbyNoJoin;
import com.broll.networklib.network.nt.NT_LobbyPlayerInfo;
import com.broll.networklib.network.nt.NT_LobbyUpdate;
import com.broll.networklib.network.nt.NT_LobbyKicked;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LobbyConnectionSite extends ClientSite {

    private INetworkRequestAttempt<GameLobby> request;
    private GameLobby lobby;
    private Map<Integer, LobbyPlayer> players;

    public LobbyConnectionSite() {

    }

    private void initRequest(INetworkRequestAttempt<GameLobby> request, GameLobby lobby){
        this.lobby = lobby;
        this.request = request;
        this.players = new HashMap<>();
    }

    public void tryJoinLobby(GameLobby lobby, String playerName, ClientAuthenticationKey secret, INetworkRequestAttempt<GameLobby> request) {
        initRequest(request, lobby);
        NT_LobbyJoin join = new NT_LobbyJoin();
        join.lobbyId = lobby.getLobbyId();
        join.authenticationKey = secret.getSecret();
        join.playerName = playerName;
        client.sendTCP(join);
    }

    public void tryCreateLobby(String playerName, Object settings, ClientAuthenticationKey secret, INetworkRequestAttempt<GameLobby> request){
        initRequest(request,null);
        NT_LobbyCreate create = new NT_LobbyCreate();
        create.playerName = playerName;
        create.authenticationKey = secret.getSecret();
        create.settings = settings;
        client.sendTCP(create);
    }

    @PackageReceiver
    public void receive(NT_LobbyUpdate lobbyUpdate) {
        //player could join lobby / create lobby / lobby update
        if(request!=null){
            if (lobby == null) {
                //player created lobby, so create client object
                lobby = new GameLobby();
                lobby.setServerIp(getClient().getConnectedIp());
            }
            //update lobby info
            LobbyLookupSite.updateLobbyInfo(lobby, lobbyUpdate);
            updateLobbyPlayers(lobbyUpdate.players);
            if (!lobby.isPlayerJoined()) {
                joinLobby();
            }
            LobbyUpdateListener listener = lobby.getLobbyUpdateListener();
            if (listener != null) {
                listener.lobbyUpdated();
            }
        }
    }

    @PackageReceiver
    public void receive(NT_ChatMessage chat) {
        if (lobby != null) {
            ChatMessageListener listener = lobby.getChatMessageListener();
            if (listener != null) {
                if (chat.from == null) {
                    //message from system
                    listener.fromGame(chat.message);
                } else {
                    //from player
                    lobby.getPlayer(chat.from).ifPresent(player -> listener.fromPlayer(chat.message, player));
                }
            }
        }
    }

    @PackageReceiver
    public void receive(NT_LobbyKicked kicked) {
        //player was removed
        if(lobby!=null && lobby.getLobbyUpdateListener()!=null){
           lobby.getLobbyUpdateListener().kickedFromLobby();
        }
        resetLobby();
    }

    @PackageReceiver
    public void receive(NT_LobbyNoJoin noJoin) {
        //could not join / create lobby
        if (request != null) {
            request.failure(noJoin.reason);
            request = null;
        }
        resetLobby();
    }

    @PackageReceiver
    public void receive(NT_LobbyClosed closed) {
        //lobby was closed
        if(lobby!=null && lobby.getLobbyUpdateListener()!=null){
            lobby.getLobbyUpdateListener().closed();
        }
        resetLobby();
    }

    private void resetLobby(){
        lobby = null;
        players.clear();
    }

    private void joinLobby() {
        players.clear();
        lobby.setPlayerJoined(true);
        lobby.setPlayers(players);
        //first update joins the player, so forward to request
        request.receive(lobby);
        request = null;
    }

    private void updateLobbyPlayers(NT_LobbyPlayerInfo[] playersInfo) {
        for (NT_LobbyPlayerInfo player : playersInfo) {
            int id = player.id;
            LobbyPlayer lobbyPlayer = players.get(id);
            if (lobbyPlayer == null) {
                playerJoined(player);
            } else {
                updatePlayer(lobbyPlayer, player);
            }
        }
        List<Integer> existingPlayers = Arrays.stream(playersInfo).map(p -> p.id).collect(Collectors.toList());
        players.entrySet().stream().filter(entry -> !existingPlayers.contains(entry.getKey())).forEach(entry -> playerLeft(entry.getValue(), entry.getKey()));
    }

    private void updatePlayer(LobbyPlayer player, NT_LobbyPlayerInfo info) {
        player.setName(info.name);
        player.setSettings(info.settings);
    }

    private void playerJoined(NT_LobbyPlayerInfo playerInfo) {
        LobbyPlayer player = new LobbyPlayer();
        updatePlayer(player, playerInfo);
        players.put(playerInfo.id, player);
        LobbyUpdateListener listener = lobby.getLobbyUpdateListener();
        if (listener != null) {
            listener.playerJoined(player);
        }
    }

    private void playerLeft(LobbyPlayer player, int id) {
        players.remove(id);
        LobbyUpdateListener listener = lobby.getLobbyUpdateListener();
        if (listener != null) {
            listener.playerLeft(player);
        }
    }

}
