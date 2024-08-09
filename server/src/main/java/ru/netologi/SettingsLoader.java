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
        Path confFilePath = Paths.get(confPath);

        if (!Files.exists(confFilePath)) {
            throw new IOException("Settings file not found: " + confFilePath.toAbsolutePath());
        }

        try (var inputStream = Files.newInputStream(confFilePath)) {
            properties.load(inputStream);
        }
//        loadCommands(commandsPath);
        Path commandFilePath = Paths.get(commandsPath);
        if (!Files.exists(commandFilePath)) {
            throw new IOException("Commands file not found: " + commandFilePath.toAbsolutePath());
        }
        List<String> lines = Files.readAllLines(commandFilePath);
        commands.addAll(lines);


    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("port", "12345"));
    }

    public HashSet<String> getCommands() {
        return commands;
    }

}
