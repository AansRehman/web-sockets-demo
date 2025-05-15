package com.poshmaals.webscokets_sample.controllers;

import com.poshmaals.webscokets_sample.config.ChatSocketHandler;
import com.poshmaals.webscokets_sample.config.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class ServerMessageController {

    private final ChatSocketHandler chatSocketHandler;

    @Autowired
    public ServerMessageController(ChatSocketHandler chatSocketHandler) {
        this.chatSocketHandler = chatSocketHandler;
    }

    @PostMapping("/send-to-client")
    public String sendToClient(@RequestParam String clientId, @RequestParam String message) {
        try {
            if (!chatSocketHandler.hasClient(clientId)) {
                return "Client not connected: " + clientId;
            }
            chatSocketHandler.sendMessageToClient(clientId, message);
            return "Message sent to client: " + clientId;
        } catch (Exception e) {
            return "Failed to send message: " + e.getMessage();
        }
    }

    @GetMapping("/connected-clients")
    public List<ChatSocketHandler.Client> getConnectedClients() {
        return chatSocketHandler.getConnectedClients();
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessageToClient(
            @RequestParam String clientName,
            @RequestParam String message) {

        WebSocketServer.sendMessageToClient(clientName, message);
        return ResponseEntity.ok("Message sent to " + clientName);
    }
}
