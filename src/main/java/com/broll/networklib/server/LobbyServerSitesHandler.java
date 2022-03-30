package com.broll.networklib.server;

import com.broll.networklib.server.impl.LobbyHandler;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ILobbyData;
import com.broll.networklib.server.impl.ServerLobby;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LobbyServerSitesHandler<L extends ILobbyData, P extends ILobbyData> extends ServerSitesHandler {

    private Map<Method, RestrictionType> receiverRestrictions = new HashMap<>();

    private LobbyHandler<L, P> lobbyHandler;

    public void setLobbyHandler(LobbyHandler<L, P> lobbyHandler) {
        this.lobbyHandler = lobbyHandler;
    }

    @Override
    protected void registerContainerRoute(ObjectTargetContainer container, Class<ServerSite> type, Method receiverMethod) {
        super.registerContainerRoute(container, type, receiverMethod);
        ConnectionRestriction packageRestriction = receiverMethod.getAnnotation(ConnectionRestriction.class);
        RestrictionType restrictionType = RestrictionType.IN_LOBBY;
        if (packageRestriction != null) {
            restrictionType = packageRestriction.value();
        }
        receiverRestrictions.put(receiverMethod, restrictionType);
    }

    @Override
    protected boolean shouldInvokeReceiver(NetworkConnection connection, ServerSite site, Method receiver, Object object) {
        RestrictionType restrictionType = receiverRestrictions.get(receiver);
        if (restrictionType == RestrictionType.NONE) {
            return true;
        }
        Player player = connection.getPlayer();
        ServerLobby lobby = null;
        if (player != null) {
            lobby = player.getServerLobby();
        }
        switch (restrictionType) {
            case PLAYER_CONNECTED:
                return player != null;
            case IN_LOBBY:
                return lobby != null;
            case NOT_IN_LOBBY:
                return lobby == null;
            case PLAYER_NOT_CONNECTED:
                return player == null;
            case LOBBY_LOCKED:
                if (lobby != null) return lobby.isLocked();
            case LOBBY_UNLOCKED:
                if (lobby != null) return !lobby.isLocked();
        }
        return false;
    }

}
