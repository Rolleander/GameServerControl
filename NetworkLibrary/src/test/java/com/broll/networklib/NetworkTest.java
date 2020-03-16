package com.broll.networklib;

import com.broll.networklib.server.GameServer;

import org.junit.Test;

public class NetworkTest {

    @Test
    public void test(){
        GameServer server = new GameServer();
        
        server.open();
    }
}
