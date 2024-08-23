package ru.netologi;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ChatClient {
    private Socket socket;
    //    private PrintWriter out;
//    private BufferedReader in;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
    private final String serverAddress;
    private String clientName;
    private final int serverPort;
    private String sessionID = "";

    public ChatClient()throws IOException {
        Initializer initializer = new Initializer();
        initializer.initialize();
        this.serverAddress = initializer.getServerAddress();
        this.serverPort = initializer.getServerPort();
        this.clientName = "Guest";
    }

    public void start() {
        try {
            connectToServer();
            startListeningForMessages();
            handleUserInput(); // в отдельный поток
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private void connectToServer() throws IOException, ClassNotFoundException {
        socket = new Socket(serverAddress, serverPort);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Connected to the chat server");

        while (true) {
            String username;
            Message serverMessage = (Message) in.readObject();
            if (sessionID.isEmpty()) {
                sessionID = serverMessage.getSessionId();
            }
            if (serverMessage.getCategory().equals("SERVICE") && serverMessage.getContent().startsWith("Введите")) {
                System.out.println(serverMessage.getContent());
                username = consoleReader.readLine().trim();
                if (username.equalsIgnoreCase("\\exit")) {
                    out.writeObject(new Message(clientName, sessionID, "SERVICE", 500, "\\exit"));
                    closeEverything("Вы решили уйти по-английски");
                    return;
                }
                System.out.println("Я ввёл имя: " + username);
                out.writeObject(new Message(clientName, sessionID, "SERVICE", 102, username));
            } else if (serverMessage.getCategory().equals("SERVICE") && serverMessage.getServiceCode() == 200) {
                clientName = serverMessage.getClientName();
                System.out.println("Вы успешно подключились к чату.");
                break;
            } else if (serverMessage.getCategory().equals("SERVICE") && serverMessage.getContent().startsWith("ERROR")) {
                System.out.println(serverMessage.getContent());
            }
        }
    }

    private void startListeningForMessages() {
        Thread listenerThread = new Thread(() -> {
            try {
                Message message;
                while ((message = (Message) in.readObject()) != null) {
                    if (message.getCategory().equalsIgnoreCase("Service")) {
                        handleServiceMessage(message.getServiceCode(), message.getContent());
                    } else if (message.getCategory().equalsIgnoreCase("Chat")) {
                        System.out.println(message.getTimestamp() + " " + message.getClientName() + ": " + message.getContent());
                    }
                }
            } catch (EOFException | SocketException e) {
                System.out.println(e.getClass()+ "\n");
//                e.printStackTrace();
                closeEverything("Потеря соединения");

// TODO Потом доработать поытку повторного подключения.
            } catch (IOException e) {
                System.out.println("An error occurred while listening for messages: " + e.getMessage());
                closeEverything("Потеря соединения");
            } catch (ClassNotFoundException e) {
                System.err.println("Критическая ошибка: Не найден необходимый класс. Программа завершает работу." + e.getMessage());
            }
        });
        listenerThread.start();
    }

    private void handleServiceMessage(int serviceCode, String Content) {
        if (serviceCode == 500 || serviceCode == 400 || serviceCode == 401) {
            System.out.println(Content);
            closeEverything(Content);
        } else {
            System.out.println(Content);
        }
    }

    private void handleUserInput() throws IOException {
        String userInput;
        while (!socket.isClosed()) {
            if (System.in.available() > 0) {
                userInput = consoleReader.readLine();
                if (userInput.equalsIgnoreCase("\\exit")) {
                    closeEverything("Попросили выйти.");
                }
                if (!userInput.trim().isEmpty()) {
                    out.writeObject(new Message(clientName, sessionID, "CHAT", 102, userInput));
                    out.flush();
                }
            }
        }
    }

    private void closeEverything(String message) {
        try {
            // Проверить, надо ли тут всё закрывать, если сокет уже закрыт.
            if (socket != null && !socket.isClosed()) socket.close();
            consoleReader.close();
            System.out.println(message);
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии ресурсов: " + e.getMessage());
        }
    }
}