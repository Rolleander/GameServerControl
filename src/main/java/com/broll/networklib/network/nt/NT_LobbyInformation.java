package com.broll.networklib.network.nt;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class NT_LobbyInformation  {

    public String lobbyName;
    public int lobbyId;
    public int playerCount;
    public int playerLimit;
    public Object settings;
}
