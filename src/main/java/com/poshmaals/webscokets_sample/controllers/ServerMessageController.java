package com.poshmaals.webscokets_sample.controllers;

import com.poshmaals.webscokets_sample.config.ChatSocketHandler;
import com.poshmaals.webscokets_sample.config.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/message")
public class ServerMessageController {

    private static final Logger log = LoggerFactory.getLogger(ServerMessageController.class);
    private final ChatSocketHandler chatSocketHandler;

    @Autowired
    public ServerMessageController(ChatSocketHandler chatSocketHandler) {
        this.chatSocketHandler = chatSocketHandler;
    }

    @PostMapping("/send-to-client")
    public List<String> sendToClient(@RequestParam(required = false) List<String> clientIds, @RequestParam String message) {
        try {
            List<String> responseMessages = new ArrayList<>();
            if(clientIds != null && clientIds.size() > 0) {
                for (String clientId : clientIds) {
                    if (!chatSocketHandler.hasClient(clientId)) {
                        throw new RuntimeException("Client not connected: " + clientId);
                    }

                    chatSocketHandler.sendMessageToClient(clientId, message);

                    responseMessages.add("Message sent to client: " + clientIds);
//                    responseMessages.add("Message sent to client: id "+ client.getId()+ "name {}" + client.getName());

                }
            }else {
                List<ChatSocketHandler.Client> clients = chatSocketHandler.getConnectedClients();
                for (ChatSocketHandler.Client client : clients) {
                    if (!chatSocketHandler.hasClient(client.getId())) {
                        throw new RuntimeException("Client not connected: " + client.getId());
                    }

                    chatSocketHandler.sendMessageToClient(client.getId(), message);

                    responseMessages.add("Message sent to client: id "+ client.getId()+ "name {}" + client.getName());
                }
            }
            return responseMessages;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/connected-clients")
    public List<ChatSocketHandler.Client> getConnectedClients() {
        return chatSocketHandler.getConnectedClients();
    }

}
