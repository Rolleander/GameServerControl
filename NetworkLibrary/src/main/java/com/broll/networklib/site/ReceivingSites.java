package com.broll.networklib.site;

import java.util.List;

public interface ReceivingSites<T extends NetworkSite> {
     void receivers(List<T> sites);
}
