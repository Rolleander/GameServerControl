package com.broll.networklib.site;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.client.ClientSite;
import com.broll.networklib.client.GameClient;
import com.broll.networklib.network.AnnotationScanner;
import com.broll.networklib.server.NetworkConnection;
import com.broll.networklib.server.ServerSite;
import com.broll.networklib.site.AbstractSitesHandler;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.minlog.Log;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.MultiValueMap;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiSitesHandler<T extends NetworkSite, C> extends AbstractSitesHandler<T, C> {

    private Map<C, Map<Class<T>, T>> activeSites = new HashMap<>();

    private final Kryo kryo = new Kryo();

    private synchronized <O> O clone(O o) {
        return kryo.copyShallow(o);
    }

    @Override
    public Map<Class, T> getSiteInstances(C connection) {
        Map<Class, T> instances = new HashMap<>();
        siteModificationLock.readLock().lock();
        instances.putAll(activeSites.get(connection));
        siteModificationLock.readLock().unlock();
        return instances;
    }

    @Override
    protected void putSite(T site) {
        super.putSite(site);
        activeSites.values().forEach(sites -> sites.put((Class<T>) site.getClass(), clone(site)));
    }

    @Override
    protected void removeSite(T site) {
        Iterator<Map.Entry<Class, ObjectTargetContainer>> iterator = siteRoutes.entrySet().iterator();
        while (iterator.hasNext()) {
            MultiTargetContainer container = (MultiTargetContainer) iterator.next().getValue();
            Iterator<Map.Entry<Class<T>, Object>> entries = container.sites.entrySet().iterator();
            while (entries.hasNext()) {
                if (site.getClass() == entries.next().getKey()) {
                    entries.remove();
                }
            }
            if (container.sites.isEmpty()) {
                iterator.remove();
            }
        }
        activeSites.values().forEach(sites -> sites.remove(site.getClass()));
    }

    @Override
    public void initConnection(C connection) {
        siteModificationLock.writeLock().lock();
        activeSites.put(connection, clone(sites));
        siteModificationLock.writeLock().unlock();
    }

    @Override
    public void discardConnection(C connection) {
        siteModificationLock.writeLock().lock();
        activeSites.remove(connection);
        siteModificationLock.writeLock().unlock();
    }

    @Override
    protected AbstractSitesHandler<T, C>.ObjectTargetContainer createContainer() {
        return new MultiTargetContainer();
    }

    @Override
    protected void registerContainerRoute(AbstractSitesHandler<T, C>.ObjectTargetContainer container, Class<T> type, Method receiverMethod) {
        ((MultiTargetContainer) container).sites.put(type, receiverMethod);
    }

    private class MultiTargetContainer extends AbstractSitesHandler<T, C>.ObjectTargetContainer {
        private MultiValueMap<Class<T>, Method> sites = new MultiValueMap<>();

        @Override
        protected Collection<T> getTargetInstances(C connectionContext) {
            return sites.keySet().stream().map(siteClass -> activeSites.get(connectionContext).get(siteClass)).collect(Collectors.toList());
        }

        @Override
        protected Collection<Method> getTargetMethods(T site) {
            return sites.getCollection(site.getClass());
        }
    }
}


