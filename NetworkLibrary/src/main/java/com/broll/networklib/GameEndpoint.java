package com.broll.networklib;

import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.network.NetworkRegistry;
import com.broll.networklib.network.INetworkRequestAttempt;
import com.broll.networklib.site.NetworkSite;
import com.broll.networklib.site.ReceivingSites;
import com.broll.networklib.site.SitesHandler;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;

import java.util.List;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GameEndpoint<T extends NetworkSite, C> implements NetworkRegister {

    private SitesHandler<T, C> sites = new SitesHandler<>();
    private IRegisterNetwork registerNetwork;

    public GameEndpoint(IRegisterNetwork registerNetwork) {
        this.registerNetwork = registerNetwork;
    }

    public IRegisterNetwork getRegisterNetwork() {
        return registerNetwork;
    }

    protected void replaceHandler(SitesHandler<T, C> sites) {
        this.sites = sites;
    }

    public void registerNetworkPackage(String packagePath) {
        NetworkRegistry.register(getKryo(), packagePath);
    }

    public void registerNetworkType(Class type) {
        Log.info("Register " + type);
        getKryo().register(type);
    }

    protected void init() {
        NetworkRegistry.registerStandard(this);
        registerNetwork.register(this);
    }

    protected void passAllSites(ReceivingSites<T> receivers) {
        receivers.receivers(sites.getSites());
    }

    protected void passReceived(C connectionContext, Object object, ReceivingSites<T> receiverSites) {
        sites.pass(connectionContext, object, receiverSites);
    }

    public void register(T... sites) {
        Arrays.asList(sites).forEach(site -> {
            this.sites.add(site);
            site.init(this);
        });
    }

    public void unregister(T... sites) {
        Arrays.asList(sites).forEach(site -> this.sites.remove(site));
    }

    public List<T> getRegisteredSites() {
        return this.sites.getSites();
    }

    public static void attemptRequest(INetworkRequestAttempt request, Runnable attempt) {
        try {
            attempt.run();
        } catch (Exception e) {
            request.failure(e.getMessage());
        }
    }

    public abstract void shutdown();

}
