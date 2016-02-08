package com.broll.gameserver.player;

public class Player {

	private String name;
	private final int id;
	
	
	public Player(int id) {
		this.id=id;
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
}
