package com.app.picchu;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private List<String> pendingUsernames;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();


    // Initializes the adapter with a list of pending friend request usernames
    public FriendRequestAdapter(List<String> pendingUsernames) {
        this.pendingUsernames = pendingUsernames;
    }


    // Inflates the layout for individual friend request items and creates a view holder for it
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_request_item, parent, false);
        return new ViewHolder(view);
    }


    // Binds the friend request data (username or UID) to the view holder and sets up click listeners for accepting or rejecting the request
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String usernameOrUid = pendingUsernames.get(position);
        holder.usernameTextView.setText(usernameOrUid);

        // "Yes" button - accept friend request
        holder.yesButton.setOnClickListener(v -> {
            acceptFriendRequest(usernameOrUid, holder);  // Function to accept the friend request
        });

        // "No" button - reject friend request
        holder.noButton.setOnClickListener(v -> {
            rejectFriendRequest(usernameOrUid, holder);  // Function to reject the friend request
        });
    }

    @Override
    public int getItemCount() {
        return pendingUsernames.size();
    }

    // ViewHolder class for friend request items
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        Button yesButton;
        Button noButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            yesButton = itemView.findViewById(R.id.yesButton);
            noButton = itemView.findViewById(R.id.noButton);
        }
    }

    // Method to accept the friend request
    private void acceptFriendRequest(String fromUid, ViewHolder holder) {

        db.collection("users").document(currentUserEmail)
                .update("friends", FieldValue.arrayUnion(fromUid))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(fromUid)
                            .update("friends", FieldValue.arrayUnion(currentUserEmail))
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(holder.itemView.getContext(), "Friend added!", Toast.LENGTH_SHORT).show();

                                removeFriendRequest(fromUid, holder);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FriendRequestAdapter", "Error adding current user to friend's list", e);
                                Toast.makeText(holder.itemView.getContext(), "Failed to add to friend's list", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FriendRequestAdapter", "Error adding friend", e);
                    Toast.makeText(holder.itemView.getContext(), "Error accepting request", Toast.LENGTH_SHORT).show();
                });
    }


    // Method to reject the friend request
    private void rejectFriendRequest(String fromUid, ViewHolder holder) {
        removeFriendRequest(fromUid, holder);
        Toast.makeText(holder.itemView.getContext(), "Friend request rejected", Toast.LENGTH_SHORT).show();
    }

    // Method to remove the friend request
    private void removeFriendRequest(String fromUid, ViewHolder holder) {
        db.collection("users").document(currentUserEmail)
                .update("friendRequests", FieldValue.arrayRemove(new HashMap<String, Object>() {{
                    put("from", fromUid);
                    put("status", "pending");
                }}))
                .addOnSuccessListener(aVoid -> {
                    pendingUsernames.remove(fromUid);  // Remove the username from the list
                    notifyDataSetChanged();  // Refresh the RecyclerView
                })
                .addOnFailureListener(e -> {
                    Log.e("FriendRequestAdapter", "Error removing friend request", e);
                });
    }
}
