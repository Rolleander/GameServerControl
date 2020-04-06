package com.broll.networklib;

import com.broll.networklib.server.GameServer;
import com.broll.networklib.server.LobbyGameServer;
import com.broll.networklib.server.LobbyServerSite;
import com.broll.networklib.server.ServerSite;
import com.broll.networklib.server.impl.LobbyHandler;
import com.esotericsoftware.minlog.Log;

import junit.framework.Test;

public class TestServer {

    public static void main(String[] args) {
        Log.INFO();
        GameServer server = new GameServer();
        server.register(new TestServerSite());
        server.open();

    }

    public static class TestServerSite extends ServerSite {

        @PackageReceiver
        public void receive(TestPackage pkg){
            Log.info("received msg: "+pkg.msg);
            pkg.msg = "receive this u noob";
            getConnection().sendTCP(pkg);
            server.shutdown();
        }

    }

    private static class LobbyData{

    }

    private static class PlayerData{

    }

    public static class TestSite extends LobbyServerSite<LobbyData, PlayerData>{

        public void trest(){

        }
    }
}
