package com.broll.gameserver.player;

public interface ClientConnection {

	public void close();
	
	public void send(Object message);
	
	
	
}
