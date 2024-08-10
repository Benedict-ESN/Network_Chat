package ru.netologi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private final int port;
    private final HashSet<String> commands;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private ServerSocket serverSocket = null;

    //    private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    public ChatServer(int port, HashSet<String> commands) {
        this.port = port;
        this.commands = commands;
    }


    public void start() {
        // Проверка, что порт не занят
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
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Прибыл новый клиент");
                    ClientHandler clientHandler = new ClientHandler(clientSocket, commands);
                    new Thread(clientHandler).start();
                } catch (SocketException e) {
                    if ("Socket closed".equals(e.getMessage())) {
                        // Сокет был закрыт намеренно, выходим из цикла
                        break;
                    } else {
                        throw e; // Неизвестная ошибка, выбрасываем исключение дальше
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Работу закончили: " + e.getMessage());
        }
    }

    // Метод для корректного завершения работы сервера
    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            pool.shutdownNow();
            System.out.println("Работу закончили: Server stopped.");
        } catch (SocketException e1) {
            System.err.println("Error closing server: " + e1.getMessage());
        } catch (IOException e2) {
            System.err.println("Error closing server: " + e2.getMessage());
        }
    }

    // Поток для обработки ввода с консоли
    private void handleConsoleInput() {
        try (var scanner = new java.util.Scanner(System.in)) {
            while (true) {
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("exit")) {
                    stop();
// TODO Реализовать корректное отключение всех клиентов при выключении сервера
                    break;
                }
            }
        }
    }


}