package com.broll.networklib.server;

import com.broll.networklib.network.AnnotationScanner;
import com.broll.networklib.server.impl.LobbyHandler;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ILobbyData;
import com.broll.networklib.server.impl.ServerLobby;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class LobbyServerSite<L extends ILobbyData, P extends ILobbyData> extends ServerSite {

    private final static Logger Log = LoggerFactory.getLogger(LobbyServerSite.class);
    protected LobbyHandler<L, P> lobbyHandler;
    protected LobbyGameServer<L, P> lobbyGameServer;
    private List<SharedField> sharedFields = new ArrayList<>();

    public void init(LobbyGameServer<L, P> lobbyServer, LobbyHandler<L, P> lobbyHandler) {
        this.lobbyGameServer = lobbyServer;
        this.lobbyHandler = lobbyHandler;
        AnnotationScanner.findAnnotatedFields(this, Autoshared.class).forEach(finding ->
                initSharedField(finding.getLeft(), finding.getRight())
        );
    }

    private void initSharedField(Field field, Autoshared shared) {
        field.setAccessible(true);
        SharedField sharedField = new SharedField();
        sharedField.field = field;
        sharedField.shareLevel = shared.value();
        sharedField.dataClass = field.getType();
        //key must be unique for site
        sharedField.key = this.getClass().getName() + ":" + field.getType().getName() + ":" + field.getName();
        sharedFields.add(sharedField);
    }

    @Override
    public void receive(NetworkConnection connection, Object object) {
        super.receive(connection, object);
        sharedFields.forEach(shared -> {
            try {
                shared.field.set(this, getSharedField(shared.key, shared.dataClass, shared.shareLevel));
            } catch (IllegalAccessException e) {
                Log.error("Failed to set shared field " + shared.field, e);
            }
        });
    }

    private Object getSharedField(String key, Class dataClass, ShareLevel shareLevel) {
        Map<String, Object> sharedStorage = getSharedStorage(shareLevel);
        if (sharedStorage == null) {
            return null;
        }
        Object object = sharedStorage.get(key);
        if (object == null) {
            //create
            object = instantiateClass(dataClass);
            sharedStorage.put(key, object);
        }
        return object;
    }

    private Object instantiateClass(Class dataClass) {
        try {
            if (dataClass.isMemberClass()) {
                //inner class default constructor with outer object
                Constructor constructor = dataClass.getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                return constructor.newInstance(this);
            } else {
                //default constructor
                return dataClass.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Log.error("Failed to init shared field data " + dataClass, e);
        }
        return null;
    }

    private Map<String, Object> getSharedStorage(ShareLevel shareLevel) {
        switch (shareLevel) {
            case LOBBY:
                ServerLobby lobby = getLobby();
                if (lobby != null) {
                    return lobby.getSharedData();
                }
                return null;
            case PLAYER:
                return getPlayer().getSharedData();
            case SERVER:
                return lobbyGameServer.getSharedData();
        }
        throw new RuntimeException("unknown sharelevel " + shareLevel);
    }

    protected Player<P> getPlayer() {
        return getConnection().getPlayer();
    }

    protected ServerLobby<L, P> getLobby() {
        Player<P> player = getPlayer();
        if (player != null) {
            return player.getServerLobby();
        }
        return null;
    }

    private class SharedField {
        public Field field;
        public Class dataClass;
        public ShareLevel shareLevel;
        public String key;
    }
}
