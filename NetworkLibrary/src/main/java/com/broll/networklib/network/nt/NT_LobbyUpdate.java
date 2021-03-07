package com.broll.networklib.network.nt;

public class NT_LobbyUpdate extends NT_LobbyInformation{

    private final static int NO_OWNER = -1;
    public NT_LobbyPlayerInfo[] players;
    public int owner = NO_OWNER;

}
