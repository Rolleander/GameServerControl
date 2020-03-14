package com.broll.networklib.site;

import com.broll.networklib.GameEndpoint;

public abstract class NetworkSite<T extends GameEndpoint> {

    private T endpoint;
    //private PingStats sitePing

    public void init(T endpoint){
        this.endpoint = endpoint;
    }

}
