package com.poshmaals.webscokets_sample.config;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap; // cientID or clientName here we are using clientName


@ServerEndpoint("/chat")
public class WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    //    static hashMap to store connections
    private static ConcurrentHashMap<String, Session> clientConnections = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("clientName") String clientName) {
        clientConnections.put(clientName, session);
        System.out.println("Connection opened for client: " + clientName);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("clientName") String clientName) {
        System.out.println("Message received from client " + clientName + ": " + message);
        try {
            Session clientSession = clientConnections.get(clientName);
            if (clientSession != null && clientSession.isOpen()) {
                clientSession.getBasicRemote().sendText("Echo: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("clientName") String clientName) { // Remove the client from the HashMap
        clientConnections.remove(clientName);
        System.out.println("Connection closed for client: " + clientName);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error occurred: " + throwable.getMessage());
    }
    // Method for the server to send a message to a specific client, this will
    // be called in API flow. where you have developed the api and password
    // clientId or name
    public static void sendMessageToClient(String clientName, String message) {
        try {
            log.info(clientConnections.toString());
            Session clientSession = clientConnections.get(clientName);
            if (clientSession != null && clientSession.isOpen()) {
                clientSession.getBasicRemote().sendText(message);
                System.out.println(
                        "Message sent to client " + clientName + ": " + message);
            } else {
                System.out.println("Client " + clientName + " is not connected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}