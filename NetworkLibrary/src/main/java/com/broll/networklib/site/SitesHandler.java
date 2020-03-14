package com.broll.networklib.site;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.server.AnnotationScanner;
import com.esotericsoftware.minlog.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SitesHandler<T extends NetworkSite> {

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

    public void pass(Object sentObject, ReceivingSites<T> receivingSites) {
        ObjectTargetContainer route = siteRoutes.get(sentObject.getClass());
        if (route == null) {
            Log.error("No receiverMethod registered for network object " + sentObject);
            return;
        }
        route.pass(sentObject, receivingSites);
    }

    private void registerRoute(Class type, T site, Method receiverMethod) {
        ObjectTargetContainer route = siteRoutes.get(type);
        if (route == null) {
            route = new ObjectTargetContainer();
            siteRoutes.put(type, route);
            typeRegister.registerType(type);
        }
        route.methods.put(site, receiverMethod);
    }

    private class ObjectTargetContainer {
        private Map<T, Method> methods = new HashMap<>();

        private void pass( Object sentObject, ReceivingSites<T> receivingSites) {
            receivingSites.receivers(new ArrayList<>(methods.keySet()));
            methods.forEach((site, method) -> pass( site, method, sentObject));
        }

        private void pass(T size, Method method, Object object) {
            try {
                method.invoke(size, object);
            } catch (Exception e) {
                Log.error("Failed to invoke receiver method "+method+" on site "+size,e);
            }
        }
    }

}
