package ru.netologi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
    private final String serverAddress;
    private final int serverPort;

    public ChatClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void start() {
        try {
            connectToServer();
            startListeningForMessages();
            handleUserInput();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            closeEverything("Мы всё завершили.");
        }
    }

    private void connectToServer() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Connected to the chat server");

        while (true) {
            String serverMessage = in.readLine();
            if (serverMessage.startsWith("SERVICE|Введите ваше имя пользователя")) {
                System.out.println(serverMessage.substring(8));
                String username = systemIn.readLine().trim();
                if (username.equalsIgnoreCase("\\exit")) {
                    out.println("\\exit");
                    closeEverything("Вы решили уйти по-английски");
                    return;
                }
                out.println(username);
            } else if (serverMessage.startsWith("SERVICE|200")) {
                System.out.println("Вы успешно подключились к чату.");
                break;
            } else if (serverMessage.startsWith("SERVICE|ERROR")) {
                System.out.println(serverMessage.substring(8));
            }
        }
    }

    private void startListeningForMessages() {
        Thread listenerThread = new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    if (serverMessage.startsWith("SERVICE|")) {
//                        System.out.println("!!!!!!!!!! "+serverMessage.substring(8));
                        handleServiceMessage(serverMessage.substring(8));
                    } else if (serverMessage.startsWith("CHAT|")) {
                        System.out.println(serverMessage.substring(5));
                    }
                }
            } catch (IOException e) {
                System.out.println("An error occurred while listening for messages: " + e.getMessage());
            }
        });
        listenerThread.start();
    }

    private void handleServiceMessage(String message) {
        if (message.equals("The connection is closed at the request of the client.") || message.equals("server is closing.")) {
            System.out.println(message);
            closeEverything(message);
        } else {
            System.out.println(message);
        }
    }

    private void handleUserInput() throws IOException {
        String userInput;
        userInput = systemIn.readLine();
//        while (!(userInput = systemIn.readLine()).equalsIgnoreCase("\\exit")) {
            out.println(userInput);
//        }
//        out.println("\\exit");
//        closeEverything();
    }

    private void closeEverything(String message) {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            if (systemIn != null) systemIn.close();
            System.out.println(message);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}