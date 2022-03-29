package com.broll.networklib.site;

import com.broll.networklib.GameEndpoint;

public abstract class NetworkSite<T extends GameEndpoint> {

    private String name;
    private T endpoint;
    //private PingStats sitePing

    public void init(T endpoint) {
        this.endpoint = endpoint;
    }


    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NetworkSite{" +
                "name='" + name + '\'' +
                ", endpoint=" + endpoint +
                '}';
    }
}
