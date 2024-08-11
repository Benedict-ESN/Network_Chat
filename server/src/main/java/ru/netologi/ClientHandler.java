package ru.netologi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    private final ConcurrentHashMap<String, ClientHandler> clientHandlers;
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final HashSet<String> commands;
    private String clientName;

    public ClientHandler(Socket socket, HashSet<String> commands, ConcurrentHashMap<String, ClientHandler> clientHandlers) throws IOException {
        this.clientSocket = socket;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.clientHandlers = clientHandlers;
        if (commands == null) {
            throw new IllegalStateException("Список команд пустой.");
        } else {
            this.commands = commands;
        }
    }
    @Override
    public void run() {
        try {
            handleClientRegistration();
            startMessageListener();
        } catch (IOException e) {
            System.err.println("Ошибка! Досвидания! \n" + e);
            closeConnection("SERVICE|Соединение закрыто сервером.");
        }
    }
    // Обработка регистрации клиента и получения его имени
    private void handleClientRegistration() throws IOException {
        while (true) {
            sendMessageToClient(this, "SERVICE|Введите ваше имя пользователя (латинские буквы, цифры, _ или -):");
            clientName = in.readLine();

            if (clientName.equalsIgnoreCase("\\exit")) {
                closeConnection("SERVICE|The connection is closed at the request of the client.");
                return;
            }

            if (clientName.isEmpty() || (isNameValid(clientName) && !isNameTaken(clientName))) {
                synchronized (clientHandlers) {
                    clientHandlers.put(clientName, this);
                }
                sendMessageToClient(this, "SERVICE|200"); // Код успешного подключения
                sendMessageToClient(this, "CHAT|Добро пожаловать в чат, " + clientName + "!");
                broadcastMessage("CHAT|" + clientName + " присоединился к чату.");
                break;
            } else {
                sendMessageToClient(this, "SERVICE|ERROR: Недопустимое имя или имя уже используется. Попробуйте другое.");
            }
        }
    }

    // Запуск отдельного потока для обработки сообщений от клиента
    private void startMessageListener() {
        Thread messageListenerThread = new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("\\exit")) {
                        closeConnection("SERVICE|Клиент отключился.");
                        break;
                    }
                    if (!message.trim().isEmpty()) {
                        if (commands.contains(message.toLowerCase())) {
                            if (!runCommand(message)) {
                                sendMessageToClient(this, "SERVICE|Команда еще не реализована.");
                            }
                        } else {
                            broadcastMessage("CHAT|"
                                    // + clientName
                                    + ": " + message);
                        }
                    }
                }
            } catch (SocketException e) {
                closeConnection("SERVICE|Соединение закрыто сервером.");
            } catch (IOException e) {
                System.err.println("Ошибка! Досвидания! \n" + e);
                closeConnection("SERVICE|Соединение закрыто сервером.");
            }
        });
        messageListenerThread.start();
    }
    private boolean isNameValid(String name) {
        // Проверка: имя состоит из допустимых символов и его длина не превышает 12 символов
        return name.matches("^[a-zA-Z0-9_-]+$") && name.length() <= 12;
    }

    private boolean runCommand(String command) {
        String msg;
        switch (command.toLowerCase()) {
            case "\\exit":
//                closeConnection("SERVICE|Клиент отключился.");
                return true;
            case "\\list":
                sendMessageToClient(this, "CHAT|" + Utils.makeCommandsList(commands));
                return true;
            case "\\help":
                msg = "CHAT|Привет. Это краткая помощь по чату. Выйти из чата: \" \\exit\". Получить список доступных команд чата: \" \\list\"";
                sendMessageToClient(this, msg);
                return true;
            case "\\users":
                synchronized (clientHandlers) {
                    msg = "CHAT|Cписок клиентов чата:" + Utils.getUsersList(clientHandlers);
                    sendMessageToClient(this, msg);
                }
                return true;
            default:
                sendMessageToClient(this, "SERVICE|Команда не распознана.");
                return false;  // Команда не распознана, но соединение не должно закрываться
        }
    }

    private void broadcastMessage(String message) {
// Получаем текущее время сервера
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

// Добавляем время и имя отправителя к сообщению
        String formattedMessage = String.format("[%s] @All:  %s: %s", timestamp, clientName, message.substring(5));
// Логируем сообщение
//        ServerLogger.log(formattedMessage);

// Выводим сообщение на сервере для администрирования
        System.out.println(formattedMessage);

        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers.values()) {
                if (clientHandler != this) { // Проверяем, не является ли это клиентом, отправившим сообщение
                    clientHandler.out.println("CHAT|" + formattedMessage);
                }
            }
        }
    }

    private void sendMessageToClient(ClientHandler targetClient, String message) {
        // Логируем сообщение перед отправкой
//        ServerLogger.log("To " + targetClient.clientName + ": " + message);
        targetClient.out.println(message);
    }

    private boolean isNameTaken(String name) {
        return clientHandlers.containsKey(name);
    }

    public void closeConnection(String reason) {
        try {
//            sendMessageToClient(this, reason);  // Сообщаем клиенту причину закрытия
            if (clientSocket != null && !clientSocket.isClosed()) {
//                in.close();
//                out.close();
                clientSocket.close();
                synchronized (clientHandlers) {
                    clientHandlers.remove(clientName);
                }
                System.out.println("SYSTEM MSG|Connection with " + clientName + " closed.");
            }
            broadcastMessage("CHAT|" + this.clientName + " покинул чат.");
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения! Досвидания! \n" + e);
        }
    }
}