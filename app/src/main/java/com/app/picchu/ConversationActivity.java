package com.app.picchu;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConversationActivity extends BaseActivity {

    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    private FirebaseFirestore db;
    private CollectionReference messagesRef;

    private String currentUserEmail;
    private String friendUID;
    private String friendEmail;
    private EditText messageInput;
    private Button sendButton;


    // Sets up the conversation activity layout, initializes Firebase and the RecyclerView for messages,
    // and sets up message input and send functionality
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        db = FirebaseFirestore.getInstance();

        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        friendEmail = getIntent().getStringExtra("friendEmail");  // Get friend UID from intent

        if (friendEmail == null || currentUserEmail == null) {
            Toast.makeText(this, "Error loading conversation", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMessages();

        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messages,currentUserEmail);
        messageRecyclerView.setAdapter(messageAdapter);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> sendMessage());

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Load messages exchanged between the current user and the friend
    private void loadMessages() {
        messagesRef = db.collection("messages");

        Query sentMessagesQuery = messagesRef
                .whereEqualTo("sender", currentUserEmail)
                .whereEqualTo("receiver", friendEmail)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        Query receivedMessagesQuery = messagesRef
                .whereEqualTo("sender", friendEmail)
                .whereEqualTo("receiver", currentUserEmail)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        sentMessagesQuery.addSnapshotListener((sentMessages, sentError) -> {
            if (sentError != null) {
                Log.e("ConversationActivity", "Error fetching sent messages: ", sentError);
                return;
            }

            receivedMessagesQuery.addSnapshotListener((receivedMessages, receivedError) -> {
                if (receivedError != null) {
                    Log.e("ConversationActivity", "Error fetching received messages: ", receivedError);
                    return;
                }

                messages.clear();

                if (sentMessages != null) {
                    for (QueryDocumentSnapshot document : sentMessages) {
                        Message message = document.toObject(Message.class);
                        messages.add(message);
                    }
                }

                if (receivedMessages != null) {
                    for (QueryDocumentSnapshot document : receivedMessages) {
                        Message message = document.toObject(Message.class);
                        messages.add(message);
                    }
                }

                Collections.sort(messages, (m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));

                messageAdapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    messageRecyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            });
        });
    }




    // Send message functionality
    private void sendMessage() {
        String messageText = messageInput.getText().toString();
        if (messageText.isEmpty()) {
            return;
        }


        Message newMessage = new Message(messageText, currentUserEmail, friendEmail);


        db.collection("messages")
                .add(newMessage)
                .addOnSuccessListener(documentReference -> {
                    messageInput.setText("");
                })
                .addOnFailureListener(e -> {
                    Log.e("ConversationActivity", "Error sending message: ", e);
                    Toast.makeText(ConversationActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }



    // Function to get email using UID
    private void getEmailFromUID(String uid, OnEmailReceivedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("friends", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String email = document.getString("email");
                            listener.onEmailReceived(email);
                        }
                    } else {
                        listener.onEmailReceived(null);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onEmailReceived(null);
                });
    }

    // Interface to handle email callback
    public interface OnEmailReceivedListener {
        void onEmailReceived(String email);
    }
}
