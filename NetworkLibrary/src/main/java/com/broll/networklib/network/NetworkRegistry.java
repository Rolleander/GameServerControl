package com.broll.networklib.network;

import com.broll.networklib.NetworkException;
import com.broll.networklib.network.nt.NT_RegisterPlayer;
import com.esotericsoftware.kryo.Kryo;
import com.google.common.reflect.ClassPath;

import java.io.IOException;

public final class NetworkRegistry {


    public final static int TCP_PORT = 54555;
    public final static int UDP_PORT = 54777;

    private final static String NT_PACKAGE = "com.broll.networklib.nt";

    private NetworkRegistry(){

    }

    public static void registerStandard(Kryo kryo){
        register(kryo,NT_PACKAGE);
    }

    public static void register(Kryo kryo, String packagePath){
        try {
            ClassPath cp = ClassPath.from(NetworkRegistry.class.getClassLoader());
            cp.getTopLevelClasses(packagePath).forEach(clazz -> kryo.register(clazz.getClass()));
        } catch (IOException e) {
            throw new NetworkException("Failed to register NT classes",e);
        }
    }
}
