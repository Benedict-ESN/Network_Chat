package ru.netologi;

import java.io.IOException;
import java.util.Properties;

public class Initializer {

    private String serverAddress;
    private int serverPort;

    public void initialize() throws IOException {
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
                throw new IOException("Initialization failed due to configuration error.");
            }
        }
        this.serverAddress = config.getProperty("serverAddress");
        this.serverPort = Integer.parseInt(config.getProperty("serverPort"));
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }
}