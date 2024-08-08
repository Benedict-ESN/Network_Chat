package ru.netologi;
import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final BufferedReader systemIn;
    private String serverAddress;
    private int serverPort;

    public ChatClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.systemIn = new BufferedReader(new InputStreamReader(System.in));
    }

    public void start() {

        try {
            connectToServer();
            handleUserInput();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            closeEverything();
        }
    }



    private void saveServerConfig() throws IOException {
        Properties props = new Properties();
        props.setProperty("serverAddress", serverAddress);
        props.setProperty("serverPort", String.valueOf(serverPort));
        props.store(new FileOutputStream("client/serverconf.conf"), null);
    }

    private void connectToServer() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Connected to the chat server");

        System.out.println("Enter your username:");
        while (true) {
            String username = systemIn.readLine().trim();
            out.println(username);
            String response = in.readLine();
            if (response.equals("Welcome to the chat, " + username + "!")) {
                System.out.println(response);
                break;
            } else {
                System.out.println(response);
            }
        }
    }

    private void handleUserInput() throws IOException {
        String userInput;
        while (!(userInput = systemIn.readLine()).equalsIgnoreCase("\\exit")) {
            out.println(userInput);
        }
        out.println("\\exit");
    }

    private void closeEverything() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            if (systemIn != null) systemIn.close();
            System.out.println("Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}