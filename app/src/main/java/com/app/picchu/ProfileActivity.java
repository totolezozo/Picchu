package com.app.picchu;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends BaseActivity {
    private static final String TAG = "ProfileActivity";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private String profileUserId;  // The UID of the user whose profile is being viewed

    private TextView pseudoText;
    private ImageView profileImage;
    private Button addFriendButton;


    // Sets up the profile activity layout, initializes Firebase instances,
    // loads the user's profile, and checks the status of friend requests
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        pseudoText = findViewById(R.id.pseudo_text);
        profileImage = findViewById(R.id.profile_image);
        addFriendButton = findViewById(R.id.add_friend_button);


        profileUserId = getIntent().getStringExtra("profileUserId");
        if (profileUserId == null || profileUserId.isEmpty()) {
            Toast.makeText(this, "Error: No profile UID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile(profileUserId);

        checkFriendRequestStatus(profileUserId);

        addFriendButton.setOnClickListener(v -> addFriend(profileUserId));
    }

    // Method to load the user profile from Firestore using UID
    private void loadUserProfile(String userId) {
        Log.d(TAG, "Loading profile for user ID: " + userId);
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String username = documentSnapshot.getString("username");
                        if (username != null) {
                            pseudoText.setText(username);
                        } else {
                            pseudoText.setText("No username available");
                        }

                        String profilePictureURL = documentSnapshot.getString("profilePictureURL");
                        if (profilePictureURL != null && !profilePictureURL.isEmpty()) {
                            Glide.with(this)
                                    .load(profilePictureURL)
                                    .placeholder(R.drawable.ic_profile_pin)
                                    .error(R.drawable.ic_profile_pin)
                                    .into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.ic_profile_pin);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user profile", e);
                    Toast.makeText(ProfileActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to send a friend request
    private void addFriend(String friendUserId) {
        Map<String, Object> friendRequest = new HashMap<>();
        friendRequest.put("from", currentUserId);
        friendRequest.put("status", "pending");

        db.collection("users").document(friendUserId)
                .update("friendRequests", FieldValue.arrayUnion(friendRequest))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                    addFriendButton.setText("Request Pending");
                    addFriendButton.setEnabled(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending friend request", e);
                    Toast.makeText(ProfileActivity.this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to check the status of the friend request and update the button accordingly
    private void checkFriendRequestStatus(String friendUserId) {
        db.collection("users").document(friendUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> friendRequests = (List<Map<String, Object>>) documentSnapshot.get("friendRequests");

                        boolean requestPending = false;
                        if (friendRequests != null) {
                            for (Map<String, Object> request : friendRequests) {
                                if (request.get("from").equals(currentUserId) && request.get("status").equals("pending")) {
                                    requestPending = true;
                                    break;
                                }
                            }
                        }

                        if (requestPending) {
                            addFriendButton.setText("Request Pending");
                            addFriendButton.setEnabled(false);
                        } else {
                            addFriendButton.setText("Add Friend");
                            addFriendButton.setOnClickListener(v -> addFriend(friendUserId));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking friend request status", e);
                });
    }

    // Method to delete a friend
    private void deleteFriend(String friendUserId) {
        db.collection("users").document(friendUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> friendRequests = (List<Map<String, Object>>) documentSnapshot.get("friendRequests");

                        if (friendRequests != null) {
                            for (Map<String, Object> request : friendRequests) {
                                if (request.get("from").equals(currentUserId)) {
                                    friendRequests.remove(request);

                                    db.collection("users").document(friendUserId)
                                            .update("friendRequests", friendRequests)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(ProfileActivity.this, "Friend request deleted", Toast.LENGTH_SHORT).show();
                                                addFriendButton.setText("Add Friend");
                                                addFriendButton.setOnClickListener(v -> addFriend(friendUserId));
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error deleting friend request", e);
                                            });

                                    break;
                                }
                            }
                        }
                    }
                });
    }

    // Method to accept a friend request
    private void acceptFriend(String friendUserId) {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> friendRequests = (List<Map<String, Object>>) documentSnapshot.get("friendRequests");

                        if (friendRequests != null) {
                            for (Map<String, Object> request : friendRequests) {
                                if (request.get("from").equals(friendUserId)) {
                                    request.put("status", "accepted");

                                    db.collection("users").document(currentUserId)
                                            .update("friendRequests", friendRequests)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(ProfileActivity.this, "Friend request accepted", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error accepting friend request", e);
                                            });

                                    break;
                                }
                            }
                        }
                    }
                });
    }

}
