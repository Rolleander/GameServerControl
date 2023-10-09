package com.broll.networklib.site;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.impl.LobbyConnectionSite;
import com.broll.networklib.network.AnnotationScanner;
import com.broll.networklib.server.impl.ConnectionSite;
import com.broll.networklib.server.impl.LobbySite;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public abstract class AbstractSitesHandler<T extends NetworkSite, C> {

    private final static Logger Log = LoggerFactory.getLogger(AbstractSitesHandler.class);
    /**
     * list of sites that are protected from clearing all sites
     */
    protected final static List<Class<? extends NetworkSite>> INTERNAL_SITES = Lists.newArrayList(ConnectionSite.class, LobbySite.class, LobbyConnectionSite.class);

    private final static IUnknownMessageReceiver DEFAULT_UNKNOWN_MESSAGE_RECEIVER = (message) -> {
        Log.error("No receiverMethod registered for network object " + message);
    };

    protected Map<Class<T>, T> sites = new HashMap<>();

    private IUnknownMessageReceiver unknownMessageReceiver = DEFAULT_UNKNOWN_MESSAGE_RECEIVER;

    protected Map<Class, ObjectTargetContainer> siteRoutes = new HashMap<>();

    protected final ReadWriteLock siteModificationLock = new ReentrantReadWriteLock();

    private SiteReceiver<T, C> siteReceiver = new SiteReceiver<>();

    public abstract Map<Class<T>, T> getSiteInstances(C connection);

    public void setReceiver(SiteReceiver<T, C> siteReceiver) {
        this.siteReceiver = siteReceiver;
    }

    public void setUnknownMessageReceiver(IUnknownMessageReceiver unknownMessageReceiver) {
        this.unknownMessageReceiver = unknownMessageReceiver;
    }

    public void clear() {
        siteModificationLock.writeLock().lock();
        new ArrayList(sites.values()).stream().filter(this::isRemovableSite).forEach(site -> {
            sites.remove(site.getClass());
            removeSite((T) site);
        });
        siteModificationLock.writeLock().unlock();
    }

    private boolean isRemovableSite(Object site){
        return INTERNAL_SITES.stream().noneMatch(it-> it.isInstance(site));
    }

    public final void add(T site) {
        siteModificationLock.writeLock().lock();
        putSite(site);
        initRoute(site);
        siteModificationLock.writeLock().unlock();
    }

    protected void putSite(T site) {
        sites.put((Class<T>) site.getClass(), site);
    }

    public final void remove(T site) {
        siteModificationLock.writeLock().lock();
        sites.remove(site.getClass());
        removeSite(site);
        siteModificationLock.writeLock().unlock();
    }

    protected abstract void removeSite(T site);

    public abstract void initConnection(C connection);

    public abstract void discardConnection(C connection);

    private void initRoute(T site) {
        AnnotationScanner.findAnnotatedMethods(site, PackageReceiver.class).forEach(m -> {
            Class<?>[] types = m.getParameterTypes();
            if (types.length== 1) {
                Class type = types[0];
                registerRoute(type, site, m);
            } else {
                Log.error("PackageReceiver method " + m + " of object " + site + " does not have correct amount of parameters (1)");
            }
        });
    }

    public final void pass(C connectionContext, Object sentObject, IReceivingSites<T> IReceivingSites) {
        siteModificationLock.readLock().lock();
        ObjectTargetContainer container = siteRoutes.get(sentObject.getClass());
        siteModificationLock.readLock().unlock();
        if (container == null) {
            if (sentObject instanceof FrameworkMessage.KeepAlive) {
            } else {
                unknownMessageReceiver.unknownMessage(sentObject);
            }
            return;
        }
        Collection<T> sites = container.getTargetInstances(connectionContext);
        IReceivingSites.receivers(sites);
        container.pass(sites, connectionContext, sentObject);
    }

    private void registerRoute(Class type, T site, Method receiverMethod) {
        ObjectTargetContainer route = siteRoutes.get(type);
        if (route == null) {
            route = createContainer();
            siteRoutes.put(type, route);
        }
        registerContainerRoute(route, (Class<T>) site.getClass(), receiverMethod);
    }

    protected abstract ObjectTargetContainer createContainer();

    protected abstract void registerContainerRoute(ObjectTargetContainer container, Class<T> type, Method receiverMethod);

    protected boolean shouldInvokeReceiver(C context, T site, Method receiver, Object object) {
        return true;
    }

    public abstract class ObjectTargetContainer {

        protected abstract Collection<T> getTargetInstances(C connectionContext);

        protected abstract Collection<Method> getTargetMethods(T site);

        protected void pass(Collection<T> instances, C connectionContext, Object sentObject) {
            instances.forEach(site -> getTargetMethods(site).stream().filter(method -> shouldInvokeReceiver(connectionContext, site, method, sentObject))
                    //must be collected in between, so filter conditions are fixed before they could be changed from invoked sites
                    .collect(Collectors.toList()).stream()
                    .forEach(method -> siteReceiver.receive(connectionContext, site, method, sentObject)));
        }
    }

}


