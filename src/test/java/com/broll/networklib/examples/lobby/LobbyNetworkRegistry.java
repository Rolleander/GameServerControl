package com.broll.networklib.examples.lobby;

import com.broll.networklib.NetworkRegister;
import com.broll.networklib.network.IRegisterNetwork;

public class LobbyNetworkRegistry implements IRegisterNetwork {
    @Override
    public void register(NetworkRegister register) {
        register.registerNetworkPackage("com.broll.networklib.examples.lobby.nt");
    }
}
