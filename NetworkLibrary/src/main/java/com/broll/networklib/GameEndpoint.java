package com.broll.networklib;

import com.broll.networklib.network.INetworkDiscoveryRequest;
import com.broll.networklib.network.NetworkRegistry;
import com.broll.networklib.network.INetworkRequestAttempt;
import com.broll.networklib.site.NetworkSite;
import com.broll.networklib.site.SitesHandler;
import com.esotericsoftware.kryo.Kryo;
import java.util.List;
import java.util.Arrays;

public abstract class GameEndpoint<T extends NetworkSite, C> implements  NetworkRegister{

    protected SitesHandler<T,C> sites = new SitesHandler<>(type -> registerNetworkType(type));

    public GameEndpoint(){
    }

    public void registerNetworkPackage(String packagePath) {
        NetworkRegistry.register(getKryo(), packagePath);
    }

    public void registerNetworkType(Class type){
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

    public void unregister(T... sites){
        Arrays.asList(sites).forEach(site -> this.sites.remove(site));
    }

    public List<T> getRegisteredSites(){
        return this.sites.getSites();
    }

    public static void attemptRequest(INetworkRequestAttempt request, Runnable attempt){
        try {
            attempt.run();
        }catch (Exception e){
            request.failure(e.getMessage());
        }
    }

    protected abstract void shutdown();

    protected abstract Kryo getKryo();

}
