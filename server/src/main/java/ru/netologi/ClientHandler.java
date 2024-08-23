package ru.netologi;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;


public class ClientHandler implements Runnable {

    private final ConcurrentHashMap<String, ClientHandler> clientHandlers;
    private final Socket clientSocket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    //    private final PrintWriter out;
//    private final BufferedReader in;
    private final HashSet<String> commands;
    private String clientName;
    private String sessionId;

    public ClientHandler(Socket socket, HashSet<String> commands, ConcurrentHashMap<String, ClientHandler> clientHandlers, String sessionId) throws IOException {
        this.clientSocket = socket;
//        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
//        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
        this.sessionId = sessionId;
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
            System.err.println("Ошибка! Досвидания! 1 \n" + e.getMessage());
            closeConnection("SERVICE|101|Соединение закрыто сервером.");
        }
    }

    // Обработка регистрации клиента и получения его имени
    private void handleClientRegistration() throws IOException {
        try {
            while (true) {
                System.out.println("Для проверки: " + sessionId);
                sendMessageToClient(this, "SERVICE|101|Введите ваше имя пользователя (латинские буквы, цифры, _ или -):");

                clientName = ((Message) in.readObject()).getContent();
                if (clientName.equalsIgnoreCase("\\exit")) {
                    closeConnection("SERVICE|500|The connection is closed at the request of the client.");
                    return;
                }
                if (isNameValid(clientName) && !isNameTaken(clientName)) {
                    synchronized (clientHandlers) {
                        clientHandlers.put(clientName, this);
                    }
                    sendMessageToClient(this, "SERVICE|200| "); // Код успешного подключения
                    sendMessageToClient(this, "CHAT|101|Добро пожаловать в чат, " + clientName + "!");
                    broadcastMessage("CHAT|102|" + clientName + " присоединился к чату.");
                    break;
                } else {
                    sendMessageToClient(this, "SERVICE|101|ERROR: Недопустимое имя или имя уже используется. Попробуйте другое.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());

        }
    }

    // Запуск отдельного потока для обработки сообщений от клиента
    private void startMessageListener() {
        Thread messageListenerThread = new Thread(() -> {
            try {
                Message receivedMessage;
                while ((receivedMessage = (Message) in.readObject()) != null) {
                    String message = receivedMessage.getContent();

                    if (message.equalsIgnoreCase("\\exit")) {
                        closeConnection("SERVICE|101|Клиент отключился.");
                        break;
                    }
                    if (!message.trim().isEmpty()) {
                        if (commands.contains(message.toLowerCase())) {
                            if (!runCommand(message)) {
                                sendMessageToClient(this, "SERVICE|101|Команда еще не реализована.");
                            }
                        } else {
                            broadcastMessage("CHAT|102|"
                                    // + clientName
                                    + ": " + message);
                        }
                    }
                }
            } catch (EOFException | SocketException e) {
                closeConnection("SERVICE|101|Соединение закрыто сервером.");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
// Если на клиенте дают команду /exit, выбрасывается именно эта ошибка, вместо нормальной обработки.
                System.err.println("Ошибка! Досвидания! 2 \n" + e);
                closeConnection("SERVICE|101|Соединение закрыто сервером.");

            }
        });
        messageListenerThread.start();
    }

    private boolean isNameValid(String name) {
        // Проверка: имя состоит из допустимых символов и его длина не превышает 12 символов
        return name.matches("^[a-zA-Z0-9_-]+$") && name.length() <= 12;
    }

    private boolean runCommand(String command) throws IOException {
        String msg;
        switch (command.toLowerCase()) {
            case "\\exit":
//                closeConnection("SERVICE|Клиент отключился.");
                return true;
            case "\\list":
                sendMessageToClient(this, "SERVICE|101|Список команд чата" + Utils.makeCommandsList(commands));
                return true;
            case "\\help":
                msg = "SERVICE|101|Привет. Это краткая помощь по чату. \n Выйти из чата: \" \\exit\". \nПолучить список доступных команд чата: \" \\list\"";
                sendMessageToClient(this, msg);
                return true;
            case "\\users":
                synchronized (clientHandlers) {
                    msg = "SERVICE|101|Cписок клиентов чата: " + Utils.getUsersList(clientHandlers);
                    sendMessageToClient(this, msg);
                }
                return true;
            default:
//                sendMessageToClient(this, "SERVICE|Команда не распознана.");
                return false;  // Команда не распознана, но соединение не должно закрываться
        }
    }

    private Message makeFormattedMessage(String message) {
// Получаем текущее время сервера
//      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int serviceCode;
        String sender;
        String[] parts = message.split("\\|", 3); // "\\|" используется для экранирования символа |
        String category = parts[0]; // Первая часть до |
        try {
            serviceCode = Integer.parseInt(parts[1]); // Вторая часть (int)
        } catch (NumberFormatException e) {
            // Обработка ошибки, если не удалось преобразовать в int
            serviceCode = 101;
            System.err.println("Ошибка: Мы где-то потеряли сервисный код.");

        }
        String content = parts[2];  // Третья часть после - сообщение
        if (category.equals("SERVICE")) {
            sender = "@InternalServerMessager";
        } else {
            sender = clientName;
        }
        ;
        return new Message(sender, sessionId, category, serviceCode, content);
    }

    private void sendMessageToClient(ClientHandler targetClient, String message) throws IOException {
        targetClient.out.writeObject(makeFormattedMessage(message));
        targetClient.out.flush();
    }

    private void broadcastMessage(String message) throws IOException {

// Логируем сообщение
        ServerLogger.log(message);

// Выводим сообщение на сервере для администрирования
        System.out.println("@All: " + message);

        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers.values()) {
                if (clientHandler != this) { // Проверяем, не является ли это клиентом, отправившим сообщение
                    try {
                        sendMessageToClient(clientHandler, message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    // endOfFileException при принудительном закрытии сокета.
    private boolean isNameTaken(String name) {
        return clientHandlers.containsKey(name);
    }

    public void closeConnection(String reason) {
        try {
//            sendMessageToClient(this, reason);  // Сообщаем клиенту причину закрытия
            if (clientSocket != null && !clientSocket.isClosed()) {
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