package com.broll.networklib.network;

import com.broll.networklib.NetworkRegister;
import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.network.nt.NT_LobbyPlayerInfo;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.esotericsoftware.minlog.Log;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.HashMap;

public final class NetworkRegistry {


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
            cp.getTopLevelClasses(packagePath).forEach(clazz -> {
                Class<?> loadedClass = clazz.load();
                Log.trace("Register " + loadedClass);
                kryo.register(loadedClass);
            });
        } catch (IOException e) {
            throw new NetworkException("Failed to register NT classes", e);
        }
    }

}
