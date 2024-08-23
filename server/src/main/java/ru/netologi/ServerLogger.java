package ru.netologi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerLogger {
    private static final Logger logger;

    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("server/src/main/resources/logging.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger = Logger.getLogger("ChatServerLogger");
        try {
            // Создаем обработчик файла для записи логов. Посмотреть file.propert...
            FileHandler fileHandler = new FileHandler("server/server-log.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Отключаем вывод в консоль
        } catch (IOException e) {
            System.err.println("Не удалось настроить логгер: " + e.getMessage());
        }
    }

    // Метод для логирования сообщений в формате Message
    public static void log(Message message) {
        logger.info(message.toString());
    }

    // Метод для логирования простых строк (например, системных сообщений)
    public static void log(String message) {
        logger.info(message);
    }
}