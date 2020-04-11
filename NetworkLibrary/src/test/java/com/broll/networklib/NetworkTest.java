package com.broll.networklib;

import com.broll.networklib.client.LobbyGameClient;
import com.broll.networklib.client.impl.GameLobby;
import com.broll.networklib.client.impl.ILobbyDiscovery;
import com.broll.networklib.server.GameServer;
import com.broll.networklib.server.LobbyGameServer;
import com.broll.networklib.server.impl.LobbySettings;
import com.esotericsoftware.minlog.Log;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NetworkTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        Log.INFO();
        LobbyGameClient client = new LobbyGameClient();
        client.listLobbies("localhost", new ILobbyDiscovery() {
            @Override
            public void discovered(String serverIp, String serverName, List<GameLobby> lobbies) {
                assertEquals("localhost", serverIp);
                assertEquals(1, lobbies.size());
                assertEquals("testLobby", lobbies.get(0).getName());
            }

            @Override
            public void noLobbiesDiscovered() {
                fail();
            }

            @Override
            public void discoveryDone() {
            }
        }).get();
        client.shutdown();
    }


}
