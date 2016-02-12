package com.broll.gameserver.sites;

import com.broll.gameserver.ServerControl;
import com.broll.gameserver.player.Player;


public interface ServerSite {

	public void receive(ServerControl server, Object message, Player from);
	
}
