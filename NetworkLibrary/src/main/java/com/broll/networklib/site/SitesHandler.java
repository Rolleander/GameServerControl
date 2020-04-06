package com.broll.networklib.site;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.AnnotationScanner;
import com.esotericsoftware.minlog.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SitesHandler<T extends NetworkSite, C> {

    private final TypeRegister typeRegister;
    private List<T> sites = new ArrayList<>();

    private Map<Class, ObjectTargetContainer> siteRoutes = new HashMap<>();

    public SitesHandler(TypeRegister typeRegister) {
        this.typeRegister = typeRegister;
    }

    public void add(T site) {
        initRoute(site);
        sites.add(site);
    }

    public void remove(T site) {
        sites.remove(site);
        Iterator<Map.Entry<Class, ObjectTargetContainer>> iterator = siteRoutes.entrySet().iterator();
        while (iterator.hasNext()) {
            ObjectTargetContainer container = iterator.next().getValue();
            Iterator<Map.Entry<T, List<Method>>> entries = container.sites.entrySet().iterator();
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
        ObjectTargetContainer route = siteRoutes.get(sentObject.getClass());
        if (route == null) {
            Log.error("No receiverMethod registered for network object " + sentObject);
            return;
        }
        route.pass(connectionContext, sentObject, receivingSites);
    }

    protected void registerRoute(Class type, T site, Method receiverMethod) {
        ObjectTargetContainer route = siteRoutes.get(type);
        if (route == null) {
            route = new ObjectTargetContainer();
            siteRoutes.put(type, route);
            typeRegister.registerType(type);
        }
        List<Method> methods = route.sites.get(site);
        if(methods==null){
            methods =new ArrayList<>();
            route.sites.put(site, methods);
        }
        methods.add(receiverMethod);
    }

    protected void invokeReceiver(C context, T site, Method receiver, Object object) {
        try {
            receiver.invoke(site, object);
        } catch (Exception e) {
            Log.error("Exception when invoking receiver method " + receiver + " on site " + site, e);
        }
    }


    private class ObjectTargetContainer {
        private Map<T, List<Method>> sites = new HashMap<>();

        private void pass(C connectionContext, Object sentObject, ReceivingSites<T> receivingSites) {
            receivingSites.receivers(new ArrayList<>(sites.keySet()));
            sites.forEach((site, methods) -> methods.forEach(method ->
                    invokeReceiver(connectionContext, site, method, sentObject)));
        }

    }

}
