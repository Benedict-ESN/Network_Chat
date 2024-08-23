package ru.netologi;

public class Main {
    public static void main(String[] args) {

        try {
            ChatClient client = new ChatClient();
            client.start();
        } catch (Exception ex) {
            System.err.println("An unexpected error occurred: " + ex);
            System.exit(1);
        }

    }
}
