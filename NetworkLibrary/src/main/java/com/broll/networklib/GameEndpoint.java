package com.broll.networklib;

import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.network.NetworkRegistry;
import com.broll.networklib.network.INetworkRequestAttempt;
import com.broll.networklib.site.AbstractSitesHandler;
import com.broll.networklib.site.NetworkSite;
import com.broll.networklib.site.ReceivingSites;
import com.broll.networklib.site.SiteReceiver;
import com.broll.networklib.site.UnknownMessageReceiver;
import com.esotericsoftware.minlog.Log;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class GameEndpoint<T extends NetworkSite, C> implements NetworkRegister {

    private AbstractSitesHandler<T, C> sites;
    private IRegisterNetwork registerNetwork;

    public GameEndpoint(IRegisterNetwork registerNetwork, AbstractSitesHandler<T, C> sitesHandler) {
        this.registerNetwork = registerNetwork;
        this.sites = sitesHandler;
    }

    public IRegisterNetwork getRegisterNetwork() {
        return registerNetwork;
    }

    public void setSiteReceiver(SiteReceiver<T, C> receiver) {
        sites.setReceiver(receiver);
    }

    public void setSitesHandler(AbstractSitesHandler<T, C> sites) {
        this.sites = sites;
    }

    public void registerNetworkPackage(String packagePath) {
        NetworkRegistry.register(getKryo(), packagePath);
    }

    public void registerNetworkType(Class type) {
        Log.trace("Register " + type);
        getKryo().register(type);
    }

    public Map<Class<T>, T> getSiteInstances(C context) {
        return sites.getSiteInstances(context);
    }

    protected void init() {
        NetworkRegistry.registerStandard(this);
        registerNetwork.register(this);
    }

    protected void initConnection(C connectionContext) {
        sites.initConnection(connectionContext);
    }

    protected void discardConnection(C connectionContext) {
        sites.discardConnection(connectionContext);
    }

    public void setUnknownMessageReceiver(UnknownMessageReceiver receiver) {
        sites.setUnknownMessageReceiver(receiver);
    }

    protected void passAllSites(C connectionContext, ReceivingSites<T> receivers) {
        receivers.receivers(sites.getSiteInstances(connectionContext).values());
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

    public static void attemptRequest(INetworkRequestAttempt request, AttemptRunnable attempt) {
        try {
            attempt.run();
        } catch (Exception e) {
            request.failure(e.getMessage());
        }
    }

    public interface AttemptRunnable {
        void run() throws Exception;
    }

    public abstract void shutdown();

}
