package com.broll.networklib.server.impl;

public class LobbyPlayer<P extends ILobbyData> {

    private Player<P> player;
    private boolean active = true;

    private int id;
    private String name;

    private boolean bot;

    private P data;

    public LobbyPlayer(Player<P> player){
        this.player = player;
        this.id = player.getId();
        this.name = player.getName();
        this.data  = player.getData();
        this.bot = player instanceof BotPlayer;
        player.setLobbyPlayer(this);
    }

    void leftLobby(){
        active  = false;
        player.setLobbyPlayer(null);
        player = null;
    }

    public boolean hasLeftLobby(){
        return !active;
    }

    public void sendTCP(Object message){
        if(active){
            player.sendTCP(message);
        }
    }

    public void sendUDP(Object message){
        if(active){
            player.sendUDP(message);
        }
    }

    public void setData(P data) {
        if(active){
            player.setData(data);
        }
        this.data = data;
    }

    public P getData() {
        if(active){
            return player.getData();
        }
        return data;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        if(active){
            return player.getName();
        }
        return name;
    }

    public boolean isOnline() {
        if(active){
            return player.isOnline();
        }
       return false;
    }

    public boolean isBot() {
        return bot;
    }
}
