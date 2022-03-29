package com.broll.networklib.client.impl;

public class LobbyPlayer {

    private int id;
    private String name;
    private Object settings;
    private GameLobby lobby;
    private boolean bot;
    private boolean me;

    LobbyPlayer(int id, GameLobby lobby) {
        super();
        this.id = id;
        this.lobby = lobby;
    }

    public GameLobby getLobby() {
        return lobby;
    }

    public boolean isBot() {
        return bot;
    }

    public int getId() {
        return id;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Object getSettings() {
        return settings;
    }

    void setSettings(Object settings) {
        this.settings = settings;
    }

    void setBot(boolean bot) {
        this.bot = bot;
    }

    void setMe(boolean me) {
        this.me = me;
    }

    public boolean isMe() {
        return me;
    }
}
