package com.broll.networklib.site;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.AnnotationScanner;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.minlog.Log;

import org.apache.commons.collections4.map.MultiValueMap;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class SitesHandler<T extends NetworkSite, C> {

    private final static UnknownMessageReceiver DEFAULT_UNKNOWN_MESSAGE_RECEIVER = (message) -> {
        Log.error("No receiverMethod registered for network object " + message);
    };

    private List<T> sites = new ArrayList<>();

    private UnknownMessageReceiver unknownMessageReceiver = DEFAULT_UNKNOWN_MESSAGE_RECEIVER;

    private Map<Class, ObjectTargetContainer> siteRoutes = new HashMap<>();

    private final ReadWriteLock siteModificationLock = new ReentrantReadWriteLock();

    public SitesHandler() {
    }

    public void setUnknownMessageReceiver(UnknownMessageReceiver unknownMessageReceiver) {
        this.unknownMessageReceiver = unknownMessageReceiver;
    }

    public void add(T site) {
        siteModificationLock.writeLock().lock();
        initRoute(site);
        sites.add(site);
        siteModificationLock.writeLock().unlock();
    }

    public void remove(T site) {
        siteModificationLock.writeLock().lock();
        sites.remove(site);
        Iterator<Map.Entry<Class, ObjectTargetContainer>> iterator = siteRoutes.entrySet().iterator();
        while (iterator.hasNext()) {
            ObjectTargetContainer container = iterator.next().getValue();
            Iterator<Map.Entry<T, Object>> entries = container.sites.entrySet().iterator();
            while (entries.hasNext()) {
                if (site == entries.next().getKey()) {
                    entries.remove();
                }
            }
            if (container.sites.isEmpty()) {
                iterator.remove();
            }
        }
        siteModificationLock.writeLock().unlock();
    }

    private void initRoute(T site) {
        AnnotationScanner.findAnnotatedMethods(site, PackageReceiver.class).forEach(m -> {
            if (m.getParameterCount() == 1) {
                Parameter p = m.getParameters()[0];
                Class type = p.getType();
                registerRoute(type, site, m);
            } else {
                Log.error("PackageReceiver method " + m + " of object " + site + " has not correct amount of parameters (1)");
            }
        });
    }

    public List<T> getSites() {
        return sites;
    }

    public void pass(C connectionContext, Object sentObject, ReceivingSites<T> receivingSites) {
        siteModificationLock.readLock().lock();
        ObjectTargetContainer route = siteRoutes.get(sentObject.getClass());
        if (route == null) {
            if (sentObject instanceof FrameworkMessage.KeepAlive) {
            } else {
                unknownMessageReceiver.unknownMessage(sentObject);
            }
            return;
        }
        receivingSites.receivers(new ArrayList<>(route.sites.keySet()));
        route.pass(connectionContext, sentObject);
        siteModificationLock.readLock().unlock();
    }

    protected void registerRoute(Class type, T site, Method receiverMethod) {
        ObjectTargetContainer route = siteRoutes.get(type);
        if (route == null) {
            route = new ObjectTargetContainer();
            siteRoutes.put(type, route);
        }
        route.sites.put(site, receiverMethod);
    }

    private void invokeReceiver(C context, T site, Method receiver, Object object) {
        try {
            receiver.invoke(site, object);
        } catch (Exception e) {
            Log.error("Exception when invoking receiver method " + receiver + " on site " + site, e);
        }
    }

    protected boolean shouldInvokeReceiver(C context, T site, Method receiver, Object object) {
        return true;
    }

    private class ObjectTargetContainer {
        private MultiValueMap<T, Method> sites = new MultiValueMap<>();

        private void pass(C connectionContext, Object sentObject) {
            sites.keySet().forEach(site -> sites.getCollection(site).stream().filter(method -> shouldInvokeReceiver(connectionContext, site, method, sentObject)).collect(Collectors.toList()).stream()
                    .forEach(method ->
                            invokeReceiver(connectionContext, site, method, sentObject)));
        }

    }
}


