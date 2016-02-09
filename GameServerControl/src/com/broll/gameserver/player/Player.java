package com.broll.gameserver.player;

import com.broll.gameserver.room.ServerRoom;

public class Player {

	private String name;
	private final int id;
	private boolean online;
	private ClientConnection connection;
	private ServerRoom room;
	
	public Player(int id, ClientConnection connection) {
		this.id = id;
		this.connection = connection;
	}

	public void updateOnlineStatus(boolean online) {
		this.online = online;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isOnline() {
		return online;
	}

	public ClientConnection getConnection() {
		return connection;
	}
	
	public ServerRoom getRoom() {
		return room;
	}
	
	

}
