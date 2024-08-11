package ru.netologi;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {

    public static String makeCommandsList(HashSet<String> commands) {
        StringBuilder sb = new StringBuilder();
        for (String cmd : commands) {
            if (!sb.isEmpty()) sb.append("; ");  // добавляем разделитель между командами
            sb.append(cmd);
        }
        return sb.toString();
    }
    public static String getUsersList(ConcurrentHashMap<String, ClientHandler> clientHandlers) {
        StringBuilder clientNames = new StringBuilder();

        for (String clientName : clientHandlers.keySet()) {
            if (!clientNames.isEmpty()) {
                clientNames.append(", ");
            }
            clientNames.append("@").append(clientName);
                }
        return clientNames.toString();
    }




}
