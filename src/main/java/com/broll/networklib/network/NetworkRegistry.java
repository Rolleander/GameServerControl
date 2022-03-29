package com.broll.networklib.network;

import com.broll.networklib.NetworkRegister;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_LobbyPlayerInfo;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
        network.registerNetworkPackage(NT_PACKAGE);
        network.registerNetworkType(NT_LobbyInformation[].class);
        network.registerNetworkType(NT_LobbyPlayerInfo[].class);
        network.registerNetworkType(Object.class);
        //   Kryo kryo = network.getKryo();
        //    MapSerializer serializer = new MapSerializer();
        //     serializer.setKeyClass(String.class, kryo.getSerializer(String.class));
        //     serializer.setValueClass(Object.class, kryo.getSerializer(Object.class));
        //     serializer.setKeysCanBeNull(false);
        //     kryo.register(HashMap.class, serializer);
    }

    public static void register(Kryo kryo, String packagePath) {
        try {
            ClassPath cp = ClassPath.from(NetworkRegistry.class.getClassLoader());
            List<Class> classes = new ArrayList<>();
            cp.getTopLevelClasses(packagePath).forEach(c->classes.add(c.load()));
            Collections.sort(classes,(c1,c2)->c1.getName().compareTo(c2.getName()));
            classes.forEach(loadedClass->{
                Log.trace("Register " + loadedClass);
                kryo.register(loadedClass);
            });
        } catch (IOException e) {
            throw new NetworkException("Failed to register network classes", e);
        }
    }

}
