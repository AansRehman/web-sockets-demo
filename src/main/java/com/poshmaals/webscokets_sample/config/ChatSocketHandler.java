package com.poshmaals.webscokets_sample.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poshmaals.webscokets_sample.model.ChatMessage;
import com.poshmaals.webscokets_sample.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();
    private final Map<String, Client> registeredClients = new ConcurrentHashMap<>();
    private final List<ChatResponse> chatHistory = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("clientSessions: "+clientSessions);
        // Initial connection doesn't have client ID yet
        clientSessions.put(session.getId(), session);
        log.info("New connection established: {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> json = objectMapper.readValue(message.getPayload(), Map.class);

            // Client registration
            if (json.containsKey("clientId") && json.containsKey("clientName")) {
                String clientId = json.get("clientId").toString();
                String clientName = json.get("clientName").toString();

                // Register client
                registeredClients.put(clientId, new Client(clientId, clientName));
                clientSessions.put(clientId, session);

                // Notify all clients about new connection
                ChatResponse notification = new ChatResponse(
                        new Client("SERVER", "System"),
                        clientName + " has joined the chat",
                        LocalDateTime.now().toString(),
                        true
                );
                broadcastMessage(notification);

                // Send welcome message and history to the new client
                ChatResponse welcome = new ChatResponse(
                        new Client("SERVER", "System"),
                        "Welcome to the chat, " + clientName,
                        LocalDateTime.now().toString(),
                        false
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcome)));

                for (ChatResponse msg : chatHistory) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
                }
                return;
            }

            // Handle private message
            if (json.containsKey("recipientId")) {
                String recipientId = json.get("recipientId").toString();
                String senderId = json.get("senderId").toString();
                String content = json.get("message").toString();

                Client sender = registeredClients.get(senderId);
                if (sender == null) {
                    throw new Exception("Sender not registered");
                }

                // Send private message to recipient
                if (clientSessions.containsKey(recipientId)) {
                    ChatResponse privateMsg = new ChatResponse(
                            sender,
                            content,
                            LocalDateTime.now().toString(),
                            false
                    );
                    clientSessions.get(recipientId).sendMessage(
                            new TextMessage(objectMapper.writeValueAsString(privateMsg))
                    );

                    // Also send to sender (so they see their own message)
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(privateMsg)));
                }
                return;
            }

            // Handle broadcast message
            ChatMessage chat = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            Client sender = registeredClients.get(chat.getSenderId());
            if (sender == null) {
                throw new Exception("Sender not registered");
            }

            ChatResponse response = new ChatResponse(
                    sender,
                    chat.getMessage(),
                    LocalDateTime.now().toString(),
                    false
            );

            // Add to history and broadcast
            chatHistory.add(response);
            broadcastMessage(response);

        } catch (Exception e) {
            log.error("Error handling message: {}", e.getMessage());
            session.sendMessage(new TextMessage("{\"error\":\"" + e.getMessage() + "\"}"));
        }
    }

    private void broadcastMessage(ChatResponse message) throws Exception {
        for (WebSocketSession s : clientSessions.values()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Find which client disconnected
        Optional<String> clientId = registeredClients.entrySet().stream()
                .filter(entry -> clientSessions.get(entry.getKey()) == session)
                .map(Map.Entry::getKey)
                .findFirst();

        if (clientId.isPresent()) {
            Client disconnectedClient = registeredClients.get(clientId.get());
            registeredClients.remove(clientId.get());
            clientSessions.remove(clientId.get());

            try {
                // Notify all clients about disconnection
                ChatResponse notification = new ChatResponse(
                        new Client("SERVER", "System"),
                        disconnectedClient.getName() + " has left the chat",
                        LocalDateTime.now().toString(),
                        true
                );
                broadcastMessage(notification);
            } catch (Exception e) {
                log.error("Error sending disconnect notification: {}", e.getMessage());
            }
        }
    }

    public void sendMessageToClient(String clientId, String message) throws Exception {
        WebSocketSession session = clientSessions.get(clientId);
        if (session != null && session.isOpen()) {
            ChatResponse response = new ChatResponse(
                    new Client("SERVER", "System"),
                    message,
                    LocalDateTime.now().toString(),
                    true
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }
    }

    public boolean hasClient(String clientId) {
        log.info(clientSessions.toString());
        return clientSessions.containsKey(clientId);
    }

    public List<Client> getConnectedClients() {
        return new ArrayList<>(registeredClients.values());
    }

    static class ChatResponse {
        private Client sender;
        private String message;
        private String timestamp;
        private boolean isSystemMessage;

        public ChatResponse(Client sender, String message, String timestamp, boolean isSystemMessage) {
            this.sender = sender;
            this.message = message;
            this.timestamp = timestamp;
            this.isSystemMessage = isSystemMessage;
        }

        // Getters and setters
        public Client getSender() { return sender; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
        public boolean isSystemMessage() { return isSystemMessage; }
        // Setters if needed
    }

    public static class Client {
        private String id;
        private String name;

        public Client(String id, String name) {
            this.id = id;
            this.name = name;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
    }
}