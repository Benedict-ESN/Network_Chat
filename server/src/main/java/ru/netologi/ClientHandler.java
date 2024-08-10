package ru.netologi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    private static final ConcurrentHashMap<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final HashSet<String> commands;
    private String clientName;

    public ClientHandler(Socket socket, HashSet<String> commands) throws IOException {
        this.clientSocket = socket;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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

                if (clientName == null || clientName.equalsIgnoreCase("\\exit")) {
                    closeConnection("SERVICE|Соединение закрыто по запросу клиента.");
                    return;
                }

                if (isNameValid(clientName) && !isNameTaken(clientName)) {
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
                if (message.equalsIgnoreCase("\\exit")) {
                    closeConnection("SERVICE|Клиент отключился.");
                    break;
                }

                if (commands.contains(message.toLowerCase())) {
                    if (!runCommand(message)) {
                        sendMessageToClient(this, "SERVICE|Команда еще не реализована.");
                    }
                } else {
                    broadcastMessage("CHAT|" + clientName + ": " + message);
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
        switch (command.toLowerCase()) {
            case "/exit":
                closeConnection("SERVICE|Клиент отключился.");
                return true;
            case "/list":
                sendMessageToClient(this, "CHAT|" + Utils.makeCommandsList(commands));
                return true;
            case "/help":
                String msg = "CHAT|Привет. Это краткая помощь по чату. \n Выйти из чата: \" \\exit\" \n Получить список команд чата: \" \\list\"";
                sendMessageToClient(this, msg);
                return true;
            default:
                return false;  // Команда еще не реализована
        }
    }
// serverMessage.substring(5)
    private void broadcastMessage(String message) {
        System.out.println("Broadcasting message: " + message.substring(5)); // Вывод сообщения в консоль сервера
        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers.values()) {
                clientHandler.out.println(message);
            }
        }
    }

    private void sendMessageToClient(ClientHandler targetClient, String message) {
        targetClient.out.println(message);
    }

    private boolean isNameTaken(String name) {
        return clientHandlers.containsKey(name);
    }

    private void closeConnection(String reason) {
        sendMessageToClient(this, reason);
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                synchronized (clientHandlers) {
                    clientHandlers.remove(this);
                }
                in.close();
                out.close();
                clientSocket.close();
                broadcastMessage("CHAT|" + this.clientName + " покинул чат.");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения! Досвидания! \n" + e);
        }
    }
}