package com.broll.networklib.server;

import com.broll.networklib.server.impl.LobbyHandler;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.LobbySettings;
import com.broll.networklib.server.impl.ServerLobby;
import com.broll.networklib.site.SitesHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LobbyServerSitesHandler<L  extends LobbySettings,P  extends LobbySettings> extends SitesHandler<ServerSite, NetworkConnection> {

    private Map<Method, RestrictionType> receiverRestrictions = new HashMap<>();

    private LobbyHandler<L,P> lobbyHandler;

    public LobbyServerSitesHandler(LobbyHandler<L,P> lobbyHandler) {
        this.lobbyHandler = lobbyHandler;
    }

    @Override
    protected void registerRoute(Class type, ServerSite site, Method receiverMethod) {
        super.registerRoute(type, site, receiverMethod);
        PackageRestriction packageRestriction = receiverMethod.getAnnotation(PackageRestriction.class);
        RestrictionType restrictionType = RestrictionType.IN_LOBBY;
        if(packageRestriction!=null){
            restrictionType = packageRestriction.value();
        }
        receiverRestrictions.put(receiverMethod, restrictionType);
    }

    @Override
    protected void invokeReceiver(NetworkConnection connection, ServerSite site, Method receiver, Object object) {
        RestrictionType restrictionType = receiverRestrictions.get(receiver);
        if(shouldInvokeReceiver(connection, restrictionType)) {
            super.invokeReceiver(connection, site, receiver, object);
        }
    }

    private boolean shouldInvokeReceiver(NetworkConnection connection, RestrictionType restrictionType){
        if(restrictionType == RestrictionType.NONE){
            return true;
        }
        Player player = connection.getPlayer();
        ServerLobby lobby = null;
        if(player!=null){
            lobby = player.getServerLobby();
        }
        switch (restrictionType){
            case PLAYER_CONNECTED: return player!=null;
            case IN_LOBBY: return lobby!=null;
            case NOT_IN_LOBBY: return lobby==null;
            case PLAYER_NOT_CONNECTED: return player==null;
            case LOBBY_LOCKED: if(lobby!=null) return lobby.isLocked();
            case LOBBY_UNLOCKED: if(lobby!=null) return !lobby.isLocked();
        }
        return false;
    }
}
