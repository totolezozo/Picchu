package com.app.picchu;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class ChatActivity extends BaseActivity {

    private RecyclerView conversationRecyclerView;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversations = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserEmail;


    // Sets up the chat activity layout, initializes the RecyclerView for displaying conversations,
    // and triggers loading of friends and conversations from Firestore
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_chat, findViewById(R.id.activity_content), true);

        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        conversationRecyclerView = findViewById(R.id.conversationRecyclerView);
        conversationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        loadFriendsAndConversations();
    }

    // Fetches the list of friends for the current user from Firestore
    // and initiates the retrieval of the last message exchanged with each friend
    private void loadFriendsAndConversations() {
        db.collection("users").document(currentUserEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> friends = (List<String>) documentSnapshot.get("friends");

                        if (friends != null) {
                            for (String friendEmail : friends) {
                                fetchLastMessageWithFriend(friendEmail);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error fetching friends list", e));
    }

    // Retrieves the most recent message exchanged between the current user and the specified friend
    // from Firestore and updates the conversations list, sorting them by timestamp
    private void fetchLastMessageWithFriend(String friendEmail) {
        db.collection("messages")
                .whereIn("sender", Arrays.asList(currentUserEmail, friendEmail))
                .whereIn("receiver", Arrays.asList(currentUserEmail, friendEmail))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)  // Get the last message exchanged
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot lastMessageDoc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String lastMessage = lastMessageDoc.getString("message");
                        Timestamp lastMessageTimestamp = lastMessageDoc.getTimestamp("timestamp");

                        String sender = lastMessageDoc.getString("sender");
                        String receiver = lastMessageDoc.getString("receiver");


                        conversations.add(new Conversation(sender, receiver, lastMessage, lastMessageTimestamp));
                    } else {
                        conversations.add(new Conversation(currentUserEmail, friendEmail, "", null));
                    }


                    Collections.sort(conversations, (c1, c2) -> {
                        if (c1.getTimestamp() == null && c2.getTimestamp() == null) {
                            return 0;
                        }
                        if (c1.getTimestamp() == null) {
                            return 1;
                        }
                        if (c2.getTimestamp() == null) {
                            return -1;
                        }
                        return c2.getTimestamp().compareTo(c1.getTimestamp());
                    });

                    conversationAdapter = new ConversationAdapter(conversations, currentUserEmail, this);
                    conversationRecyclerView.setAdapter(conversationAdapter);
                })
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error fetching last message", e));
    }

}
