package com.broll.gameserver.room;

import java.util.List;

import com.broll.gameserver.player.Player;

public class ServerRoom {

	
	private final int id;
	private String name;
	private List<Player> players;
	
	public ServerRoom(int id) {
		this.id=id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
}
