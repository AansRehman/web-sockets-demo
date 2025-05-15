package com.poshmaals.webscokets_sample.model;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatMessage {
//    private Client sender;
    private String message;
    private String senderId;

//    public Client getSender() {
//        return sender;
//    }
//
//    public void setSender(Client sender) {
//        this.sender = sender;
//    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
