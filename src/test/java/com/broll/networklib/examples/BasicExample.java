package com.broll.networklib.examples;

import com.broll.networklib.NetworkRegister;
import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.ClientSite;
import com.broll.networklib.client.GameClient;
import com.broll.networklib.server.GameServer;
import com.broll.networklib.server.NetworkConnection;
import com.broll.networklib.server.ServerSite;

public class BasicExample {

    public static void main(String[] args) {
        GameClient client = new GameClient(BasicExample::registerNetwork);
        GameServer server = new GameServer(BasicExample::registerNetwork);
        client.register(new BasicClientSite());
        server.register(new BasicServerSite());
        server.open();
        client.connect("localhost");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.shutdown();
        server.shutdown();
    }

    private static class BasicServerSite extends ServerSite{

        @Override
        public void onConnect(NetworkConnection connection) {
            super.onConnect(connection);
            NT_TestPackage testPackage = new NT_TestPackage();
            testPackage.msg = "Hello Client!";
            getConnection().sendTCP(testPackage);
        }
    }

    private static class BasicClientSite extends ClientSite {
        @PackageReceiver
        public void received(NT_TestPackage testPackage){
            System.out.println("The server told me: "+testPackage.msg);
        }
    }

    private static void registerNetwork(NetworkRegister register){
        register.registerNetworkType(NT_TestPackage.class);
    }

    private static class NT_TestPackage {
        public String msg;
    }

}
