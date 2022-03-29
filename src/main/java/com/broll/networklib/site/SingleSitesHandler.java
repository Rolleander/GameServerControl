package com.broll.networklib.site;

import com.broll.networklib.client.ClientSite;
import com.broll.networklib.client.GameClient;

import org.apache.commons.collections4.map.MultiValueMap;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SingleSitesHandler<T extends NetworkSite, C> extends AbstractSitesHandler<T, C> {

    public SingleSitesHandler() {
    }

    @Override
    public Map<Class<T>, T> getSiteInstances(C connection) {
        Map<Class<T>, T> instances = new HashMap<>();
        siteModificationLock.readLock().lock();
        instances.putAll(sites);
        siteModificationLock.readLock().unlock();
        return instances;
    }

    @Override
    protected void removeSite(T site) {
        Iterator<Map.Entry<Class, ObjectTargetContainer>> iterator = siteRoutes.entrySet().iterator();
        while (iterator.hasNext()) {
            ClientTargetContainer container = (ClientTargetContainer) iterator.next().getValue();
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
    }

    @Override
    public void initConnection(C connection) {
        //nothing to do
    }

    @Override
    public void discardConnection(C connection) {
        //nothing to do
    }

    @Override
    protected ObjectTargetContainer createContainer() {
        return new ClientTargetContainer();
    }

    private class ClientTargetContainer extends ObjectTargetContainer {
        private MultiValueMap<T, Method> sites = new MultiValueMap<>();

        @Override
        protected Collection<T> getTargetInstances(C connectionContext) {
            return sites.keySet();
        }

        @Override
        protected Collection<Method> getTargetMethods(T site) {
            return sites.getCollection(site);
        }
    }

    @Override
    protected void registerContainerRoute(ObjectTargetContainer container, Class<T> type, Method receiverMethod) {
        ((ClientTargetContainer) container).sites.put(sites.get(type), receiverMethod);
    }

}


