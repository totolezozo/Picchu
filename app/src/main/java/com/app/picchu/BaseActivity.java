package com.app.picchu;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private FirebaseFirestore db;
    private List<String> userList;
    private List<String> usernameList;
    private UserAdapter userAdapter;
    private LinearLayout topDrawer;

    // Initializes the base activity layout and Firebase connection, and sets up the UI elements
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);

        Log.d(TAG, "onCreate: BaseActivity initialized");

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        usernameList = new ArrayList<>();

        topDrawer = findViewById(R.id.top_drawer);
        setupMenu();
        setupTopDrawer();
    }


    // Sets up the navigation menu buttons (Home, Profile, Plus, Messages, and Custom) with click listeners
    // and handles navigation between different activities
    protected void setupMenu() {
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton profileButton = findViewById(R.id.profile_button);
        ImageButton plusButton = findViewById(R.id.plus_button);
        ImageButton messagesButton = findViewById(R.id.messages_button);
        ImageButton customButton = findViewById(R.id.custom_button);

        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                if (!(this instanceof MainActivity)) {
                    startActivity(new Intent(this, MainActivity.class));
                }
            });
        } else {
            Log.e(TAG, "Home button is null!");
        }

        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                if (!(this instanceof AccountActivity)) {
                    startActivity(new Intent(this, AccountActivity.class));
                }
            });
        } else {
            Log.e(TAG, "Profile button is null!");
        }

        if (plusButton != null) {
            plusButton.setOnClickListener(v -> toggleTopDrawer());
        } else {
            Log.e(TAG, "Plus button is null!");
        }

        if (messagesButton != null) {
            messagesButton.setOnClickListener(v -> {
                if (!(this instanceof ChatActivity)) {
                    startActivity(new Intent(this, ChatActivity.class));
                }
            });
        } else {
            Log.e(TAG, "Messages button is null!");
        }

        if (customButton != null) {
            customButton.setOnClickListener(v -> {
                Toast.makeText(this, "Custom Button clicked", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "Custom button is null!");
        }
    }


    // Sets up the top drawer functionality for searching users, including the RecyclerView and search input field
    private void setupTopDrawer() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_users);
        EditText searchUsername = findViewById(R.id.search_username);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            userAdapter = new UserAdapter(userList, usernameList);
            recyclerView.setAdapter(userAdapter);
        }

        if (searchUsername != null) {
            searchUsername.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().isEmpty()) {
                        searchForUsersByUsername(s.toString());
                    } else {
                        userList.clear();
                        usernameList.clear();
                        userAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }


    // Searches for users in Firestore whose username matches or starts with the input string
    // and updates the RecyclerView with the search results
    private void searchForUsersByUsername(String username) {
        String lowerCaseUsername = username.toLowerCase();
        db.collection("users")
                .whereGreaterThanOrEqualTo("username", lowerCaseUsername)
                .whereLessThanOrEqualTo("username", lowerCaseUsername + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        usernameList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userName = document.getString("username");
                            String userId = document.getId();
                            if (userName != null) {
                                usernameList.add(userName);
                                userList.add(userId);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(BaseActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    // Toggles the visibility of the top drawer with a slide-down or slide-up animation
    private void toggleTopDrawer() {
        if (topDrawer.getVisibility() == View.GONE) {
            topDrawer.setVisibility(View.VISIBLE);
            ObjectAnimator animator = ObjectAnimator.ofFloat(topDrawer, "translationY", -topDrawer.getHeight(), 0);
            animator.setDuration(300);
            animator.start();
        } else {
            ObjectAnimator animator = ObjectAnimator.ofFloat(topDrawer, "translationY", 0, -topDrawer.getHeight());
            animator.setDuration(300);
            animator.start();
            animator.addListener(new android.animation.Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    topDrawer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart(android.animation.Animator animation) {
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                }

                @Override
                public void onAnimationRepeat(android.animation.Animator animation) {
                }
            });
        }
    }
}


