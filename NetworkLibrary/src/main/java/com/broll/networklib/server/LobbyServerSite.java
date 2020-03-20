package com.broll.networklib.server;

import com.broll.networklib.server.ServerSite;
import com.broll.networklib.server.impl.LobbyHandler;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ServerLobby;
import com.esotericsoftware.kryonet.Server;

public abstract class LobbyServerSite<L, P> extends ServerSite {

    protected LobbyHandler<L, P> lobbyHandler;

    public void init(LobbyHandler<L, P> lobbyHandler){
        this.lobbyHandler = lobbyHandler;
    }

    protected Player<P> getPlayer(){
        return getConnection().getPlayer();
    }

    protected ServerLobby<L, P> getLobby(){
        Player<P> player = getPlayer();
        if(player!=null){
            return player.getServerLobby();
        }
        return null;
    }
}
