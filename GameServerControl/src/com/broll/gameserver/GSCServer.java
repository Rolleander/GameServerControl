package com.broll.gameserver;

import java.util.List;

import com.broll.gameserver.sites.ServerSite;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class GSCServer {

	private List<ServerSite> sites;
	private Server server;
	
	public GSCServer() {
		server=new Server();
		server.addListener(new Listener(){
			
		});
		
	}
	
	public void register(ServerSite site)
	{
		
	}
	
	public void start()
	{
		
	}
	
	public void close()
	{
		
	}
	
}
