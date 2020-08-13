package com.broll.networklib.client.impl;

public class LobbyPlayer {

    private String name;
    private Object settings;

    LobbyPlayer(){
        super();
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

    public void setSettings(Object settings) {
        this.settings = settings;
    }
}
