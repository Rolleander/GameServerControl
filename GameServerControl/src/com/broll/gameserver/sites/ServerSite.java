package com.broll.gameserver.sites;

import com.broll.gameserver.player.Player;

public interface ServerSite<T> {

	public void receive(T message, Player from);
	
}
