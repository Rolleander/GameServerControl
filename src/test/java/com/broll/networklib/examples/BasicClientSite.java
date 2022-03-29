package com.broll.networklib.examples;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.ClientSite;

public class BasicClientSite extends ClientSite {

    @PackageReceiver
    public void received(NT_TestPackage testPackage){
        System.out.println("The server told me: "+testPackage.msg);
    }

}
