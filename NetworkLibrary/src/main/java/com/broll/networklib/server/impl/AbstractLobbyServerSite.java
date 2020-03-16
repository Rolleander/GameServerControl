package com.broll.networklib.server.impl;

import com.broll.networklib.server.ServerSite;

abstract class AbstractLobbyServerSite extends ServerSite {

    protected LobbyHandler lobbyHandler;

    public AbstractLobbyServerSite(LobbyHandler lobbyHandler){
        this.lobbyHandler = lobbyHandler;
    }
}
