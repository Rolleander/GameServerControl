package com.broll.networklib;

import com.broll.networklib.network.NetworkRegistry;
import com.broll.networklib.site.NetworkSite;
import com.broll.networklib.site.SitesHandler;
import com.esotericsoftware.kryo.Kryo;

import java.util.Arrays;

public abstract class GameEndpoint<T extends NetworkSite> {

    protected SitesHandler<T> sites = new SitesHandler<>(type -> registerNetworkType(type));

    public GameEndpoint(){
    }

    public void registerNetworkPackage(String packagePath) {
        NetworkRegistry.register(getKryo(), packagePath);
    }

    protected void registerNetworkType(Class type){
        getKryo().register(type);
    }

    protected void init(){
        NetworkRegistry.registerStandard(getKryo());
    }

    public void register(T... sites) {
        Arrays.asList(sites).forEach(site -> {
            this.sites.add(site);
            site.init(this);
        });
    }

    protected abstract void shutdown();

    protected abstract Kryo getKryo();

}
