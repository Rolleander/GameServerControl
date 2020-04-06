package com.broll.networklib;

import com.broll.networklib.network.NetworkRegistry;

public interface NetworkRegister {

     void registerNetworkPackage(String packagePath) ;

     void registerNetworkType(Class type);
}
