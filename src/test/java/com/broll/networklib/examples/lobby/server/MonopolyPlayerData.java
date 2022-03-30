package com.broll.networklib.examples.lobby.server;

import com.broll.networklib.examples.lobby.nt.NT_PlayerSettings;
import com.broll.networklib.examples.lobby.nt.NT_TokenType;
import com.broll.networklib.server.impl.ILobbyData;
import com.broll.networklib.server.impl.ServerLobby;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

public class MonopolyPlayerData implements ILobbyData {

    private NT_TokenType tokenType;

    private boolean ready;

    public void setTokenType(NT_TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public NT_TokenType getTokenType() {
        return tokenType;
    }

    public void assignFreeToken(ServerLobby<MonopolyLobbyData, MonopolyPlayerData> lobby) {
        List<NT_TokenType> freeTokens = Lists.newArrayList(NT_TokenType.values());
        List<NT_TokenType> takenTokens = lobby.getPlayersData().stream().map(it -> it.tokenType).collect(Collectors.toList());
        freeTokens.removeAll(takenTokens);
        this.tokenType = freeTokens.get(0);
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }

    @Override
    public Object nt() {
        NT_PlayerSettings settings = new NT_PlayerSettings();
        settings.tokenType = tokenType;
        settings.ready = ready;
        return settings;
    }
}
