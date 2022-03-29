package com.broll.networklib.server;

import com.broll.networklib.site.MultiSitesHandler;

public class ServerSitesHandler extends MultiSitesHandler<ServerSite, NetworkConnection> {

    public final ServerSite accessSite(NetworkConnection connectionContext, Class<ServerSite> siteClass) {
        ServerSite site = getSiteInstances(connectionContext).get(siteClass);
        if (site == null) {
            throw new RuntimeException("No site instance exists for " + siteClass);
        }
        site.receive(connectionContext, null);
        return site;
    }
}
