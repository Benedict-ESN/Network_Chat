package ru.netologi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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
            // Запрос имени клиента
            while (true) {
                sendMessageToClient(this, "SERVICE|Введите ваше имя пользователя (латинские буквы, цифры, _ или -):");
                clientName = in.readLine();

                if (clientName.equalsIgnoreCase("\\exit")) {
                    closeConnection("SERVICE|The connection is closed at the request of the client.");
                    return;
                }

                if (clientName == null || (isNameValid(clientName) && !isNameTaken(clientName))) {
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

            // Начало обработки сообщений от клиента
            String message;
            while ((message = in.readLine()) != null) {
//                if (message.equalsIgnoreCase("\\exit")) {
//                    closeConnection("SERVICE|Клиент отключился.");
//                    break;
//                }
// TODO починить команды чата. Они не работают и игнорируются.

                if (commands.contains(message.toLowerCase())) {
                    if (!runCommand(message)) {
                        sendMessageToClient(this, "SERVICE|Команда еще не реализована.");
                    }
                } else {
                    broadcastMessage("CHAT|"
                            //+ clientName
                            + ": " + message);
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка! Досвидания! \n" + e);
        } finally {
            closeConnection("SERVICE|Соединение закрыто сервером.");
        }
    }

    private boolean isNameValid(String name) {
        return name.matches("^[a-zA-Z0-9_-]+$");
    }

    private boolean runCommand(String command) {
        String msg;
        switch (command.toLowerCase()) {
            case "\\exit":
                closeConnection("SERVICE|Клиент отключился.");
                return true;
            case "\\list":
                sendMessageToClient(this, "CHAT|" + Utils.makeCommandsList(commands));
                return true;
            case "\\help":
                msg = "CHAT|Привет. Это краткая помощь по чату. \n Выйти из чата: \" \\exit\" \n Получить список команд чата: \" \\list\"";
                sendMessageToClient(this, msg);
                return true;
            case "\\users":
// TODO реализовать список клиентов чата

                msg = "CHAT|тут будет список клиентов чата:"+Utils.makeUsersList(clientHandlers);
                sendMessageToClient(this, msg);
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
        ServerLogger.log(formattedMessage);

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
        ServerLogger.log("To " + targetClient.clientName + ": " + message);
        targetClient.out.println(message);
    }

    private boolean isNameTaken(String name) {
        return clientHandlers.containsKey(name);
    }

    public void closeConnection(String reason) {
        try {
            sendMessageToClient(this, reason);  // Сообщаем клиенту причину закрытия
            if (clientSocket != null && !clientSocket.isClosed()) {
                in.close();
                out.close();
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