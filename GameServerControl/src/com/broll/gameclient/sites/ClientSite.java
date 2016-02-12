package com.broll.gameclient.sites;

import com.broll.gameclient.ClientControl;

public interface ClientSite<T> {

	public void received(ClientControl client,Object message);
}
