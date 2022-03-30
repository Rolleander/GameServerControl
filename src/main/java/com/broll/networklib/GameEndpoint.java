package com.broll.networklib;

import com.broll.networklib.network.IRegisterNetwork;
import com.broll.networklib.network.NetworkRegistry;
import com.broll.networklib.network.INetworkRequestAttempt;
import com.broll.networklib.site.AbstractSitesHandler;
import com.broll.networklib.site.NetworkSite;
import com.broll.networklib.site.IReceivingSites;
import com.broll.networklib.site.SiteReceiver;
import com.broll.networklib.site.IUnknownMessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public abstract class GameEndpoint<T extends NetworkSite, C> implements NetworkRegister {

    private final static Logger Log = LoggerFactory.getLogger(GameEndpoint.class);
    private AbstractSitesHandler<T, C> sites;
    private IRegisterNetwork registerNetwork;

    /** @param WRITE_BUFFER_SIZE One buffer of this size is allocated. Objects are serialized to the write buffer where the bytes are
     *           queued until they can be written to the TCP socket.
     *           <p>
     *           Normally the socket is writable and the bytes are written immediately. If the socket cannot be written to and
     *           enough serialized objects are queued to overflow the buffer, then the connection will be closed.
     *           <p>
     *           The write buffer should be sized at least as large as the largest object that will be sent, plus some head room to
     *           allow for some serialized objects to be queued in case the buffer is temporarily not writable. The amount of head
     *           room needed is dependent upon the size of objects being sent and how often they are sent.
     */
    public static int WRITE_BUFFER_SIZE = 819200;

    /**
     * @param OBJECT_BUFFER_SIZE One (using only TCP) or three (using both TCP and UDP) buffers of this size are allocated. These
     *           buffers are used to hold the bytes for a single object graph until it can be sent over the network or
     *           deserialized.
     *           <p>
     *           The object buffers should be sized at least as large as the largest object that will be sent or received. */
    public static int OBJECT_BUFFER_SIZE = 204800;

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

    public void setUnknownMessageReceiver(IUnknownMessageReceiver receiver) {
        sites.setUnknownMessageReceiver(receiver);
    }

    protected void passAllSites(C connectionContext, IReceivingSites<T> receivers) {
        receivers.receivers(sites.getSiteInstances(connectionContext).values());
    }

    protected void passReceived(C connectionContext, Object object, IReceivingSites<T> receiverSites) {
        sites.pass(connectionContext, object, receiverSites);
    }

    public void clearSites(){
        sites.clear();
    }

    public void register(T... sites) {
        Log.info("Register sites "+ sites);
        Arrays.asList(sites).forEach(site -> {
            this.sites.add(site);
            site.init(this);
        });
    }

    public void unregister(T... sites) {
        Log.info("Unregister sites "+sites);
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
