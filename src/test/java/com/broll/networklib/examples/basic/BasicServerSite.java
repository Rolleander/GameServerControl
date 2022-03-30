package com.broll.networklib.examples.basic;

import com.broll.networklib.server.NetworkConnection;
import com.broll.networklib.server.ServerSite;

public class BasicServerSite extends ServerSite {

    @Override
    public void onConnect(NetworkConnection connection) {
        super.onConnect(connection);
        NT_TestPackage testPackage = new NT_TestPackage();
        testPackage.msg = "Hello Client!";
        getConnection().sendTCP(testPackage);
    }

}


