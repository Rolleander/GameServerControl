package com.broll.networklib.network;

import com.broll.networklib.NetworkRegister;
import com.broll.networklib.network.nt.NT_ChatMessage;
import com.broll.networklib.network.nt.NT_ListLobbies;
import com.broll.networklib.network.nt.NT_LobbyClosed;
import com.broll.networklib.network.nt.NT_LobbyCreate;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_LobbyJoin;
import com.broll.networklib.network.nt.NT_LobbyJoined;
import com.broll.networklib.network.nt.NT_LobbyKick;
import com.broll.networklib.network.nt.NT_LobbyKicked;
import com.broll.networklib.network.nt.NT_LobbyLeave;
import com.broll.networklib.network.nt.NT_LobbyLock;
import com.broll.networklib.network.nt.NT_LobbyNoJoin;
import com.broll.networklib.network.nt.NT_LobbyPlayerInfo;
import com.broll.networklib.network.nt.NT_LobbyReconnected;
import com.broll.networklib.network.nt.NT_LobbyUpdate;
import com.broll.networklib.network.nt.NT_ReconnectCheck;
import com.broll.networklib.network.nt.NT_ServerInformation;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public final class NetworkRegistry {

    private final static Logger Log = LoggerFactory.getLogger(NetworkRegistry.class);

    public final static int TCP_PORT = 54555;
    public final static int UDP_PORT = 54777;

    private final static String NT_PACKAGE = "com.broll.networklib.network.nt";

    private NetworkRegistry() {

    }

    public static void registerStandard(NetworkRegister network) {
        network.registerNetworkType(NT_ChatMessage.class);
        network.registerNetworkType(NT_LobbyClosed.class);
        network.registerNetworkType(NT_LobbyCreate.class);
        network.registerNetworkType(NT_LobbyInformation.class);
        network.registerNetworkType(NT_LobbyJoin.class);
        network.registerNetworkType(NT_LobbyJoined.class);
        network.registerNetworkType(NT_LobbyKick.class);
        network.registerNetworkType(NT_LobbyKicked.class);
        network.registerNetworkType(NT_LobbyLock.class);
        network.registerNetworkType(NT_LobbyNoJoin.class);
        network.registerNetworkType(NT_LobbyPlayerInfo.class);
        network.registerNetworkType(NT_LobbyReconnected.class);
        network.registerNetworkType(NT_LobbyUpdate.class);
        network.registerNetworkType(NT_LobbyLeave.class);
        network.registerNetworkType(NT_ReconnectCheck.class);
        network.registerNetworkType(NT_ListLobbies.class);
        network.registerNetworkType(NT_ServerInformation.class);
        network.registerNetworkType(NT_LobbyInformation[].class);
        network.registerNetworkType(NT_LobbyPlayerInfo[].class);
        network.registerNetworkType(Object.class);
    }

    public static void register(Kryo kryo, String packagePath) {
        try {
            ClassPath cp = ClassPath.from(NetworkRegistry.class.getClassLoader());
            List<Class> classes = new ArrayList<>();
            cp.getTopLevelClasses(packagePath).forEach(c->classes.add(c.load()));
            Collections.sort(classes, Comparator.comparing(Class::getName));
            classes.forEach(loadedClass->{
                Log.trace("Register " + loadedClass);
                kryo.register(loadedClass);
            });
        } catch (IOException e) {
            throw new NetworkException("Failed to register network classes", e);
        }
    }

}
