package com.app.picchu;

import com.google.firebase.Timestamp;

public class Message {
    private String message;
    private String sender;
    private String receiver;
    private Timestamp timestamp;
    private boolean read;

    // Default constructor for Firebase
    // Provides a no-argument constructor required for Firebase deserialization
    public Message() {}

    // Initializes a new message with the provided content, sender, and receiver, and sets the current timestamp and read status to false
    public Message(String message, String sender, String receiver) {
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = Timestamp.now();
        this.read = false;
    }

    // Getters :

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }


    // Returns whether the message has been read or not
    public boolean isRead() {
        return read;
    }


    // Sets the message as read or unread
    public void setRead(boolean read) {
        this.read = read;
    }
}

