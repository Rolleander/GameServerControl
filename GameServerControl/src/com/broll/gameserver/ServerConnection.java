package com.broll.gameserver;

import com.broll.gameserver.player.Player;
import com.esotericsoftware.kryonet.Connection;

public class ServerConnection extends Connection{

	private Player player;
	
	public void init(Player player)
	{
		this.player=player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	
}
