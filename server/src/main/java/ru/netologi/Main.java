package ru.netologi;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServer();
            server.start();
        } catch (IOException e) {
            System.err.println("Что-то пошло не так: " + e.getMessage());
            System.exit(1);
        }
    }
}