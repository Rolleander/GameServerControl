package com.broll.networklib.examples.lobby.server;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.examples.lobby.nt.NT_ChangeToken;
import com.broll.networklib.examples.lobby.nt.NT_PlayerReady;
import com.broll.networklib.examples.lobby.nt.NT_TokenType;
import com.broll.networklib.server.ConnectionRestriction;
import com.broll.networklib.server.LobbyServerSite;
import com.broll.networklib.server.RestrictionType;

public class MonopolyServerSite extends LobbyServerSite<MonopolyLobbyData, MonopolyPlayerData> {

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.LOBBY_UNLOCKED)
    public void received(NT_ChangeToken changeToken) {
        NT_TokenType newToken = changeToken.tokenType;
        if (getLobby().getPlayersData().stream().map(MonopolyPlayerData::getTokenType).anyMatch(token -> token == newToken)) {
            //cannot switch to the new token, because another player uses that already
            return;
        }
        getPlayer().getData().setTokenType(newToken);
        getLobby().sendLobbyUpdate();
    }

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.LOBBY_UNLOCKED)
    public void received(NT_PlayerReady playerReady) {
        getPlayer().getData().setReady(playerReady.ready);
        getLobby().sendLobbyUpdate();
        if (getLobby().getPlayersData().stream().map(MonopolyPlayerData::isReady).reduce(true, Boolean::logicalAnd)) {
            //all players are ready, lets start the game!
        }
    }

}
