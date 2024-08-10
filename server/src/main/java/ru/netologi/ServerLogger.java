package ru.netologi;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerLogger {
    private static final Logger logger = Logger.getLogger("ChatServerLogger");

    static {
        try {
            // Создаем обработчик файла для записи логов
            FileHandler fileHandler = new FileHandler("server/server-log.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Отключаем вывод в консоль
        } catch (IOException e) {
            System.err.println("Не удалось настроить логгер: " + e.getMessage());
        }
    }

    public static void log(String message) {
        logger.info(message);
    }
}
