package ru.netologi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private final int port;
    private final HashSet<String> commands;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private ServerSocket serverSocket = null;
    private final ConcurrentHashMap<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();

    public ChatServer(int port, HashSet<String> commands) {
        this.port = port;
        this.commands = commands;
    }

    public void start() {
        try (ServerSocket testSocket = new ServerSocket(port)) {
            // Порт свободен, можно закрыть тестовый сокет и продолжить запуск сервера
        } catch (IOException e) {
            System.err.println("Port " + port + " уже использукется. Выберете другой порт. \n" + e);
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port: " + port);
            new Thread(this::handleConsoleInput).start();

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Прибыл новый клиент");
                ClientHandler clientHandler = new ClientHandler(clientSocket, commands, clientHandlers);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Работу закончили: " + e.getMessage());
        }
    }

    // Метод для корректного завершения работы сервера
    public void stop() {
        try {
            synchronized (clientHandlers) {
                if (!clientHandlers.isEmpty()) {
                    System.out.println("Отключение всех клиентов...");
                    for (ClientHandler clientHandler : clientHandlers.values()) {
                        clientHandler.closeConnection("SERVICE|server is closing.");
                    }
                }
            }
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                pool.shutdownNow();
                System.out.println("Сервер остановлен.");
            } catch(IOException e){
                System.err.println("Error closing server: " + e.getMessage());
            }
        }

    // Поток для обработки ввода с консоли
    private void handleConsoleInput() {
        try (var scanner = new java.util.Scanner(System.in)) {
            while (true) {
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("exit")) {
                    stop();
                    break;
                }
            }
        }
    }
}