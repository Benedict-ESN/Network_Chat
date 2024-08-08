package ru.netologi;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            SettingsLoader settings = new SettingsLoader("server/settings.conf", "server/commands.conf");
            ChatServer server = new ChatServer(settings.getPort(), settings.getCommands());
            server.start();
        } catch (IOException e) {
            System.out.println("Что-то пошло не так: " + e.getMessage());
        }
    }
}