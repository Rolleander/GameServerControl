package com.broll.networklib.server;

import com.broll.networklib.server.impl.LobbySettings;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.function.Consumer;

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
        if(input==null){
            return  false;
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

    private class Command {
        public String info;
        public Consumer<List<String>> consumer;
    }
}
