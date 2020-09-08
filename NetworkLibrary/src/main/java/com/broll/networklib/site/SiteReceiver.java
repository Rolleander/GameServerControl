package com.broll.networklib.site;

import com.esotericsoftware.minlog.Log;

import org.apache.commons.lang3.time.StopWatch;

import java.lang.reflect.Method;

public class SiteReceiver<T extends NetworkSite, C> {

    public void receive(C context, T site, Method receiver, Object object) {
        try {
            if (!receiver.isAccessible()) {
                receiver.setAccessible(true);
            }
            receiver.invoke(site, object);
        } catch (Exception e) {
            Log.error("Exception when invoking receiver method " + receiver + " on site " + site, e);
        }
    }
}
