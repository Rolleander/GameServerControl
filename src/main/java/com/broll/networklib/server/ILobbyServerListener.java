package com.broll.networklib.server;

import com.broll.networklib.server.impl.ILobbyData;
import com.broll.networklib.server.impl.IServerLobbyListener;
import com.broll.networklib.server.impl.ServerLobby;

public interface ILobbyServerListener<L  extends ILobbyData,P  extends ILobbyData> extends IServerLobbyListener<L,P> {

    void lobbyOpened(ServerLobby<L,P> lobby);

}
