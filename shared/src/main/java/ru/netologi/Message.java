package ru.netologi;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private final LocalDateTime timestamp;
    private final String clientName;
    private final String sessionId;
    private final String category;  // "Server" или "Chat"
    private final String content;

    public Message(String clientName, String sessionId, String category, String message) {
        this.timestamp = LocalDateTime.now();
        this.clientName = clientName;
        this.sessionId = sessionId;
        this.category = category;
        this.content = message;
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

    @Override
    public String toString() {
        return "[" + timestamp + "] " + clientName + " (" + category + "): " + content;
    }
}