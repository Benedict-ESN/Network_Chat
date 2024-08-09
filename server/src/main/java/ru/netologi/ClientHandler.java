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
        } else {this.commands = commands;}
    }

    @Override
    public void run() {
        try {
            // Получаем имя клиента
            clientName = in.readLine();
            if (clientName == null || clientName.isBlank() || isNameTaken(clientName)) {
                out.println("ERROR: Никнейм уже используется или недопустимое имя.");
                clientSocket.close();
                return;
            }

            synchronized (clientHandlers) {
                clientHandlers.put(clientName, this);
            }

            out.println("Welcome to the chat, " + clientName + "!");
            broadcastMessage(clientName + " has joined the chat.");

            String message;
            while ((message = in.readLine()) != null) {
                if (commands.contains(message.toLowerCase())) {
                    if (!runCommand(message)) {
                        out.println("Command not implemented yet.");
                    }
                } else {
                    broadcastMessage(clientName + ": " + message);
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка! Досвидания! \n" + e);
        } finally {
            closeConnection();
        }
    }


    private boolean runCommand(String command) {
        switch (command.toLowerCase()) {
            case "/exit":
                closeConnection();
                return true;
            case "/list":

//              System.out.println("Список команд загружен:\n" + Utils.makeCommandsList(commands));
//                StringBuilder sb = new StringBuilder();
//                for (String cmd : commands) {
//                    if (!sb.isEmpty()) sb.append("\n ");  // добавляем разделитель между командами
//                    sb.append(cmd);
//                }
                sendMessageToClient(this, Utils.makeCommandsList(commands));
                return true;
            case "/help":
                String msg = "Привет. Это краткая помощь по чтау. \n Выйти их чата: \" \\exit\" \n Получить список команд чата: \" \\list\"";
                sendMessageToClient(this, msg);
                return true;
            default:
                return false;  // Команда еще не реализована
        }
    }

    private void broadcastMessage(String message) {
        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers.values()) {
                clientHandler.out.println(message);
            }
        }
    }

    private void sendMessageToClient(ClientHandler targetClient, String message) {
        if (targetClient == null || targetClient == this) {
            // Если клиент не указан, отправляем сообщение самому себе
            this.out.println("To @" + this.clientName + ": " + message);
        } else {
            // Отправляем сообщение указанному клиенту
            targetClient.out.println("Message from @" + this.clientName + ": " + message);
        }
    }

    private boolean isNameTaken(String name) {
        return clientHandlers.containsKey(name);
    }

    private void closeConnection() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                in.close();
                out.close();
                clientSocket.close();
                synchronized (clientHandlers) {
                    clientHandlers.remove(this);
                }

                broadcastMessage(clientName + " has left the chat.");
            }
        } catch (IOException e) {
            System.err.println("Ошибка! Досвидания! \n" + e);
        }
    }
}