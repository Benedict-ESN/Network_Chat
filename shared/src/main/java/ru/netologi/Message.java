package ru.netologi;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private final LocalDateTime timestamp;
    private final String clientName;
    private final String sessionId;
    private final String category;  // "Server" или "Chat"
    private final String content;
    private final int serviceCode;

    public Message(String clientName, String sessionId, String category, int serviceCode,String message) {
        this.timestamp = LocalDateTime.now();
        this.clientName = clientName;
        this.sessionId = sessionId;
        this.category = category;
        this.content = message;
        this.serviceCode = serviceCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getClientName() {
        return clientName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }
    public int getServiceCode() {
        return serviceCode;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + clientName + " (" + category + "): " + content;
    }
}