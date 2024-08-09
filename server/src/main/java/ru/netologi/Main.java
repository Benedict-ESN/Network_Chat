package ru.netologi;

import java.io.IOException;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        try {
            SettingsLoader settings = new SettingsLoader("server/settings.conf", "server/commands.conf");

            HashSet<String> commandsList =settings.getCommands();
            String formattedCommands = Utils.makeCommandsList(commandsList);
            System.out.println("Список команд загружен:\n" + formattedCommands);

            ChatServer server = new ChatServer(settings.getPort(), commandsList);
            server.start();
        } catch (IOException e) {
            System.err.println("Что-то пошло не так: " + e.getMessage());
        }
    }
}