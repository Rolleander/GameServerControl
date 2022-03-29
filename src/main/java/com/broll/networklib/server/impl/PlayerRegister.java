package com.broll.networklib.server.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerRegister {

    private AtomicInteger playerIdCounter = new AtomicInteger();

    private Map<String, Player> playerRegister = new ConcurrentHashMap<>();

    public int registerPlayerId() {
        return playerIdCounter.getAndIncrement();
    }

    public Player getPlayer(String key) {
        return playerRegister.get(key);
    }

    public void register(String key, Player player) {
        playerRegister.put(key, player);
    }

    public void unregister(String key) {
        playerRegister.remove(key);
    }

}
