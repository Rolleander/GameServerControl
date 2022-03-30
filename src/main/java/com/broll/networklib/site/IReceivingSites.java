package com.broll.networklib.site;

import java.util.Collection;
import java.util.List;

public interface IReceivingSites<T extends NetworkSite> {
     void receivers(Collection<T> sites);
}
