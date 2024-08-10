package ru.netologi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class ServerDetailsPrompter {
    public Properties askServerDetails() throws IOException {
        Properties props = new Properties();
        BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Задайте IP адрес и порт сервера в одном из следующих форматов [IP address:port] или 'localhost:port' если сервер поднят локально:");
        String input = systemIn.readLine().trim();

        String serverAddress;
        String serverPort;

        if ("localhost".equalsIgnoreCase(input)) {
            serverAddress = "localhost";
            System.out.println("Enter server port:");
            serverPort = systemIn.readLine().trim();
        } else if (input.contains(":")) {
            String[] parts = input.split(":");
            serverAddress = parts[0].trim();
            serverPort = parts.length > 1 ? parts[1].trim() : "12345";
        } else {
            serverAddress = input;
            System.out.println("Enter server port:");
            serverPort = systemIn.readLine().trim();
        }

        props.setProperty("serverAddress", serverAddress);
        props.setProperty("serverPort", serverPort);
        return props;
    }
}


