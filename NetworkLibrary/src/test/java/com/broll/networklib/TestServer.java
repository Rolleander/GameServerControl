package com.broll.networklib;

import com.broll.networklib.server.GameServer;
import com.broll.networklib.server.ServerSite;
import com.esotericsoftware.minlog.Log;

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

}
