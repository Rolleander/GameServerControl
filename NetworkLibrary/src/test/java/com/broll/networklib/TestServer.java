package com.broll.networklib;

import com.broll.networklib.network.nt.NT_LobbyInformation;
import com.broll.networklib.server.GameServer;
import com.broll.networklib.server.LobbyGameServer;
import com.broll.networklib.server.LobbyServerSite;
import com.broll.networklib.server.ServerSite;
import com.broll.networklib.server.impl.LobbyHandler;
import com.broll.networklib.server.impl.LobbySettings;
import com.esotericsoftware.minlog.Log;

import junit.framework.Test;

public class TestServer {

    public static void main(String[] args) {
        Log.INFO();
        LobbyGameServer<LobbyData, PlayerData> server = new LobbyGameServer<>("Test");
        server.open();
        server.getLobbyHandler().openLobby("testLobby");
    }

    private class LobbyData implements LobbySettings {

        @Override
        public Object getSettings() {
            return null;
        }
    }

    private class PlayerData implements LobbySettings {

        @Override
        public Object getSettings() {
            return null;
        }
    }

}
