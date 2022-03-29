package com.broll.networklib;

import com.broll.networklib.network.NetworkRegistry;
import com.esotericsoftware.kryo.Kryo;

public interface NetworkRegister {

     void registerNetworkPackage(String packagePath) ;

     void registerNetworkType(Class type);

     Kryo getKryo();
}
