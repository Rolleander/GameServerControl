package com.broll.networklib.examples;

import com.broll.networklib.NetworkRegister;
import com.broll.networklib.network.IRegisterNetwork;

public class BasicNetworkRegistry implements IRegisterNetwork {
    @Override
    public void register(NetworkRegister register) {
        register.registerNetworkType(BasicExample.class);
        register.registerNetworkPackage("com.mynetpackage");
    }
}
