package com.app.picchu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "AccountActivity";

    private ImageView profilePicture;
    private TextView userName, numberPicchu, numberFriends;
    private Button friendRequestsButton;
    private RecyclerView friendRequestsRecyclerView;
    private boolean isFriendRequestsVisible = false;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri;


    // Sets up the account page layout and initializes Firebase and UI components
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AccountActivity", "AccountActivity onCreate called");

        getLayoutInflater().inflate(R.layout.activity_account, findViewById(R.id.activity_content), true);
        Log.d("AccountActivity", "Layout inflated");

        profilePicture = findViewById(R.id.profile_picture);
        userName = findViewById(R.id.user_name);
        numberPicchu = findViewById(R.id.number_picchu);
        numberFriends = findViewById(R.id.number_friends);
        friendRequestsButton = findViewById(R.id.friend_requests_button);
        friendRequestsRecyclerView = findViewById(R.id.friend_requests_recycler_view);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        friendRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUserData();

        friendRequestsButton.setOnClickListener(v -> toggleFriendRequestsVisibility());

        loadPendingFriendRequests();

        profilePicture.setOnClickListener(v -> selectImage());
    }


    // Fetches and displays user data (name, surname, username, profile picture) from Firestore
    private void loadUserData() {
        Log.d("AccountActivity", "loadUserData called");

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Log.d("AccountActivity", "User Email: " + userEmail);

        db.collection("users").document(userEmail).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String surname = documentSnapshot.getString("surname");
                String username = documentSnapshot.getString("username");
                String profilePictureURL = documentSnapshot.getString("profilePictureURL");

                Log.d("AccountActivity", "Fetched user data - Name: " + name + ", Surname: " + surname + ", Username: " + username);

                userName.setText(name + " " + surname + " (" + username + ")");


                if (profilePictureURL != null) {
                    Glide.with(this)
                            .load(profilePictureURL)
                            .placeholder(R.drawable.ic_profile_pin)
                            .into(profilePicture);
                } else {
                    profilePicture.setImageResource(R.drawable.ic_profile_pin);
                }
            } else {
                Log.d("AccountActivity", "User document does not exist.");
            }
        }).addOnFailureListener(e -> {
            Log.e("AccountActivity", "Error retrieving user data", e);
        });
    }

    // Allows the user to select an image from their device to update the profile picture
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    // Handles the result after the user selects an image for their profile picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
    }

    // Uploads the selected image to Firebase Storage and updates the profile picture URL in Firestore
    private void uploadImage() {
        if (imageUri != null) {
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            StorageReference fileReference = storageReference.child("profile_pictures/" + userEmail + ".jpg");

            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();

                    db.collection("users").document(userEmail)
                            .update("profilePictureURL", downloadUrl)
                            .addOnSuccessListener(aVoid -> {
                                Glide.with(this)
                                        .load(downloadUrl)
                                        .placeholder(R.drawable.ic_profile_pin)
                                        .into(profilePicture);

                                Toast.makeText(AccountActivity.this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AccountActivity.this, "Failed to update profile picture: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(AccountActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    // Toggles the visibility of the friend requests RecyclerView
    private void toggleFriendRequestsVisibility() {
        isFriendRequestsVisible = !isFriendRequestsVisible;
        friendRequestsRecyclerView.setVisibility(isFriendRequestsVisible ? View.VISIBLE : View.GONE);
    }

    // Fetches and displays pending friend requests from Firestore, showing usernames or UIDs
    private void loadPendingFriendRequests() {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        List<String> pendingUsernames = new ArrayList<>();
        FriendRequestAdapter adapter = new FriendRequestAdapter(pendingUsernames);
        friendRequestsRecyclerView.setAdapter(adapter);

        db.collection("users").document(currentUserEmail).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> friendRequests = (List<Map<String, Object>>) documentSnapshot.get("friendRequests");

                if (friendRequests != null && !friendRequests.isEmpty()) {
                    Log.d(TAG, "Pending friend requests found.");

                    for (Map<String, Object> request : friendRequests) {
                        if ("pending".equals(request.get("status"))) {
                            String fromUid = (String) request.get("from");

                            db.collection("users").document(fromUid).get().addOnSuccessListener(userSnapshot -> {
                                String username = userSnapshot.getString("username");

                                if (username != null && !username.isEmpty()) {
                                    pendingUsernames.add(username);
                                } else {
                                    pendingUsernames.add(fromUid);
                                }

                                adapter.notifyDataSetChanged();
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Error fetching username", e);
                                pendingUsernames.add(fromUid);
                                adapter.notifyDataSetChanged();
                            });
                        }
                    }
                } else {
                    Log.d(TAG, "No pending friend requests.");
                    Toast.makeText(AccountActivity.this, "No pending friend requests", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "User document does not exist.");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading friend requests", e);
        });
    }
}
