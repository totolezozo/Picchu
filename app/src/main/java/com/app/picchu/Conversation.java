package com.app.picchu;

import com.google.firebase.Timestamp;

public class Conversation {
    private String sender;
    private String receiver;
    private String lastMessage;
    private Timestamp timestamp;


    // Initializes a conversation with the sender, receiver, last message, and timestamp
    public Conversation(String sender, String receiver, String lastMessage, Timestamp timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }


    // Returns the other participant's email in the conversation (either sender or receiver)
    // depending on the current user's email
    public String getSenderOrReceiver(String currentUserEmail) {
        return sender.equals(currentUserEmail) ? receiver : sender;
    }

    // Returns the last message exchanged in the conversation
    public String getLastMessage() {
        return lastMessage;
    }

    // Returns the timestamp of the last message in the conversation
    public Timestamp getTimestamp() {
        return timestamp;
    }
}
