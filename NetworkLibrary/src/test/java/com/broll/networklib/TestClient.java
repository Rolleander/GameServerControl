package com.broll.networklib;

import com.broll.networklib.client.ClientSite;
import com.broll.networklib.client.GameClient;
import com.broll.networklib.server.GameServer;
import com.esotericsoftware.minlog.Log;

import org.checkerframework.checker.units.qual.C;

import java.util.List;

public class TestClient {

    private static boolean shutdown = false;
    public static void main(String[] args) {
        Log.INFO();
        GameClient client = new GameClient();
        client.register(new TestSite());
     //   List<String> ips = client.discoverServers();
      //  if(!ips.isEmpty()){
//            String ip = ips.get(0);
        String ip = "localhost";
//            Log.info("Discovered server on "+ip);
            client.connect(ip);
            TestPackage tp =new TestPackage();
            tp.msg = "lolololol";
            client.sendTCP(tp);

     //   }
        do{
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(shutdown==false);
    }

    private static class TestSite extends ClientSite{

        @PackageReceiver
        public void received(TestPackage tp){
            Log.info("client recevied "+tp.msg);
            client.shutdown();
            shutdown = true;
        }
    }
}
