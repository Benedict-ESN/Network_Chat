package ru.netologi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class SettingsLoader {
    private final Properties properties = new Properties();
    private final HashSet<String> commands = new HashSet<>();


    public SettingsLoader(String confPath, String commandsPath) throws IOException {

        // Преобразуем строку в Path объект
        Path path = Paths.get(confPath);

        if (!Files.exists(path)) {
            throw new IOException("Settings file not found: " + path.toAbsolutePath());
        }

        try (var inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        }
//        loadCommands(commandsPath);
        Path commandPath = Paths.get(commandsPath);
        if (!Files.exists(commandPath)) {
            throw new IOException("Commands file not found: " + commandPath.toAbsolutePath());
        }
        List<String> lines = Files.readAllLines(path);
        commands.addAll(lines);


    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("port", "12345"));
    }

    public HashSet<String> getCommands() {
        return commands;
    }

}
