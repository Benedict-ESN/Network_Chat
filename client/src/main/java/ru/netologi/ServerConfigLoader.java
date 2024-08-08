package ru.netologi;

import java.io.*;
import java.util.Properties;

public class ServerConfigLoader {
    private final String configPath;

    public ServerConfigLoader(String configPath) {
        this.configPath = configPath;
    }

    public Properties loadConfig() throws IOException {
        File configFile = new File(configPath);
        Properties props = new Properties();

        if (!configFile.exists() || configFile.length() == 0) {
            System.out.println("Configuration file not found or empty at " + configFile.getAbsolutePath());
            throw new IOException("Configuration file not found or empty.");
        }

        try (InputStream inputStream = new FileInputStream(configFile)) {
            props.load(inputStream);
        }

        // Проверка наличия всех необходимых параметров
        if (props.getProperty("serverAddress") == null || props.getProperty("serverPort") == null) {
            System.out.println("Configuration file is missing necessary details.");
            throw new IOException("Configuration file is missing necessary details.");
        }

        return props;
    }

    public void saveConfig(Properties props) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(configPath)) {
            props.store(outputStream, "Server Configuration");
        }
    }
}