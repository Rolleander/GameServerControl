package com.broll.networklib.server;

import com.broll.networklib.server.impl.LobbySettings;
import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ServerLobby;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LobbyServerCLI {
    private LobbyGameServer lobbyGameServer;
    private Map<String, Command> commands = new HashMap<>();
    private boolean shutdownCalled = false;

    LobbyServerCLI(LobbyGameServer lobbyGameServer) {
        this.lobbyGameServer = lobbyGameServer;
        cmd("help", "Displays this help", options -> {
            print(">>>> The following commands are available:");
            commands.entrySet().forEach(entry -> print(entry.getKey() + " - " + entry.getValue().info));
        });
        cmd("close", "Shutdown server", options -> {
            print("Server will shutdown now");
            lobbyGameServer.shutdown();
            shutdownCalled = true;
        });
        cmd("info", "Server info", options -> {
            Collection<ServerLobby> lobbies = lobbyGameServer.getLobbyHandler().getLobbies();
            print("Server has " + lobbies.size() + " lobbies open with " + lobbies.stream().map(ServerLobby::getPlayerCount).reduce(0, Integer::sum) + " total players");
            lobbies.forEach(lobby -> {
                Collection<Player> players = lobby.getPlayers();
                print("==> [" + lobby.getId() + "] " + lobby.getName() + " with " + players.size() + " players: " + players.stream().map(Player::getName).collect(Collectors.joining(",")));
            });
        });
    }

    private void print(String s) {
        System.out.println(s);
    }

    private void cmd(String starts, String help, Consumer<List<String>> consumer) {
        Command command = new Command();
        command.info = help;
        command.consumer = consumer;
        commands.put(starts, command);
    }

    public boolean hanldeInput(String input) {
        if (input == null) {
            return false;
        }
        String[] cmd = input.toLowerCase().trim().split("\\s+");
        List<String> options = new ArrayList<>();
        if (cmd.length == 0 || StringUtils.isBlank(cmd[0])) {
            return false;
        }
        if (cmd.length > 1) {
            for (int i = 1; i < cmd.length; i++) {
                options.add(cmd[i]);
            }
        }
        commands.entrySet().stream().
                filter(entry -> entry.getKey().
                        startsWith(cmd[0])).
                forEach(entry -> entry.getValue().consumer.
                        accept(options));
        return shutdownCalled;
    }

    public static void open(LobbyGameServer server) {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
        LobbyServerCLI cli = server.initCLI();
        do {
            try {
                String input = reader.readLine();
                if (cli.hanldeInput(input)) {
                    //server stopped
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } while (true);
    }

    private class Command {
        public String info;
        public Consumer<List<String>> consumer;
    }
}
