package ru.netologi;

import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        try {
            ServerConfigLoader configLoader = new ServerConfigLoader("client/serverconf.conf");
            Properties config;
            try {
                config = configLoader.loadConfig();
            } catch (IOException e) {
                System.out.println("Error loading config: " + e.getMessage());
                ServerDetailsPrompter prompter = new ServerDetailsPrompter();
                try {
                    config = prompter.askServerDetails();
                    configLoader.saveConfig(config);
                } catch (IOException ex) {
                    System.out.println("Failed to save config: " + ex.getMessage());
                    return;
                }
            }
            String serverAddress = config.getProperty("serverAddress");
            int serverPort = Integer.parseInt(config.getProperty("serverPort"));
            ChatClient client = new ChatClient(serverAddress, serverPort);
            client.start();
        } catch (Exception ex) {
            System.err.println("An unexpected error occurred: " + ex.getMessage());
        }
    }
}
