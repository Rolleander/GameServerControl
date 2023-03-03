package com.broll.networklib.server;

import com.broll.networklib.server.impl.Player;
import com.broll.networklib.server.impl.ServerLobby;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyServerCLI {
    private LobbyGameServer lobbyGameServer;
    private Map<String, CliCommand> commands = new HashMap<>();
    private boolean shutdownCalled = false;

    LobbyServerCLI(LobbyGameServer lobbyGameServer) {
        this.lobbyGameServer = lobbyGameServer;
        Lists.newArrayList(
                cmd("help", "Displays this help", options -> {
                    print(">>>> The following commands are available:");
                    commands.entrySet().forEach(entry -> print(entry.getKey() + " - " + entry.getValue().info));
                }),
                cmd("close", "Shutdown server", options -> {
                    print("Server will shutdown now");
                    lobbyGameServer.shutdown();
                    shutdownCalled = true;
                }),
                cmd("loglevel","Changes loglevel [0=Trace, 1=Debug, 2=Info, 3=Warn, 5=Error, 6=None]",options->{
                    int level = Integer.parseInt(options.get(0));
                    com.esotericsoftware.minlog.Log.set(level);
                    System.out.println("Changed loglevel to "+level);
                }),
                cmd("info", "Server info", options -> {
                    Collection<ServerLobby> lobbies = lobbyGameServer.getLobbyHandler().getLobbies();
                    print("Server has " + lobbies.size() + " lobbies open with " + lobbies.stream().map(ServerLobby::getPlayerCount).reduce(0, Integer::sum) + " total players");
                    lobbies.forEach(lobby -> {
                        Collection<Player> players = lobby.getActivePlayers();
                        String hidden = lobby.isHidden() ? " Hidden" : "";
                        String locked = lobby.isLocked() ? " Locked" : "";
                        String full = lobby.isFull() ? " Full" : "";
                        String autoClose = lobby.isAutoClose() ? "" : " AlwaysOpen";
                        String limit = lobby.getPlayerLimit() == ServerLobby.NO_PLAYER_LIMIT ? "" : " / "+lobby.getPlayerLimit();
                        print("==> "+lobby.getName() +" [#"+lobby.getId() + locked + full + hidden + autoClose + "] with " + players.size() +limit + " players (" + players.stream().map(Player::getName).collect(Collectors.joining(","))+")");
                    });
                })
        ).forEach(this::add);
    }

    private void print(String s) {
        System.out.println(s);
    }

    private void add(CliCommand command) {
        commands.put(command.cmd, command);
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
        CliCommand foundCmd = commands.get(cmd[0]);
        if(foundCmd!=null){
            try {
                foundCmd.consumer.run(options);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            System.err.println("Command not found: \""+cmd[0]+"\" Type help for list of available commands");
        }
        return shutdownCalled;
    }

    public static CliCommand cmd(String cmd, String help, ICLIExecutor execution) {
        CliCommand command = new CliCommand();
        command.cmd = cmd;
        command.info = help;
        command.consumer = execution;
        return command;
    }

    public static void open(LobbyGameServer server, CliCommand... cmds) {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
        LobbyServerCLI cli = server.initCLI();
        Arrays.stream(cmds).forEach(cli::add);
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

    public static class CliCommand {
        public String cmd;
        public String info;
        public ICLIExecutor consumer;
    }
}
