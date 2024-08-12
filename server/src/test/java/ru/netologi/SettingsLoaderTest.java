package ru.netologi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsLoaderTest {

    private SettingsLoader settingsLoader;

    @BeforeEach
    void setUp() throws IOException {
        settingsLoader = new SettingsLoader("src/test/resources/settings.conf", "src/test/resources/commands.conf");
    }

    @Test
    void testLoadConfig() throws IOException {

        Path settingsPath = Paths.get("src/test/resources/settings.conf");
        Path commandsPath = Paths.get("src/test/resources/commands.conf");

        assertTrue(Files.exists(settingsPath));
        assertTrue(Files.exists(commandsPath));

        SettingsLoader settingsLoader = new SettingsLoader(settingsPath.toString(), commandsPath.toString());

        assertEquals(22455, settingsLoader.getPort());
        HashSet<String> commands = settingsLoader.getCommands();
        assertTrue(commands.contains("\\exit"));
        assertTrue(commands.contains("\\list"));
    }

    @Test
    void testLoadConfigFileNotFound() {
        // Проверяем, выбрасывается ли исключение при отсутствии файла настроек
        assertThrows(IOException.class, () -> {
            new SettingsLoader("nonexistent.conf", "nonexistent_commands.conf");
        });
    }

    @Test
    void testLoadPort() {
        int port = settingsLoader.getPort();
        assertEquals(22455, port);
    }

    @Test
    void testLoadCommands() {
        assertTrue(settingsLoader.getCommands().contains("\\help"));
    }

}
