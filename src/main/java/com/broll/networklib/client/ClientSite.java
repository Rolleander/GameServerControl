package com.broll.networklib.client;

import com.broll.networklib.site.NetworkSite;

public class ClientSite extends NetworkSite<GameClient> {

    protected GameClient client;

    @Override
    public void init(GameClient endpoint) {
        super.init(endpoint);
        this.client = endpoint;
    }

    public GameClient getClient() {
        return client;
    }

    public void receive(Object object){

    }

    public void onConnect(){

    }

    public void onDisconnect(){

    }
}
