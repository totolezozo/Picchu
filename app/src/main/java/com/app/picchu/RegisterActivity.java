package com.app.picchu;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailInput, usernameInput, ageInput, passwordInput, confirmPasswordInput, nameInput, surnameInput;
    private RadioButton maleRadio, femaleRadio;
    private Button registerButton;


    // Sets up the registration activity layout, initializes Firebase authentication and Firestore,
    // and sets up input fields and buttons for user registration
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailInput = findViewById(R.id.emailInput);
        nameInput = findViewById(R.id.nameInput);
        surnameInput = findViewById(R.id.surnameInput);
        usernameInput = findViewById(R.id.usernameInput);
        ageInput = findViewById(R.id.ageInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        maleRadio = findViewById(R.id.maleRadio);
        femaleRadio = findViewById(R.id.femaleRadio);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String name = nameInput.getText().toString().trim();
                String surname = surnameInput.getText().toString().trim();
                String username = usernameInput.getText().toString().trim();
                String age = ageInput.getText().toString().trim();
                String gender = maleRadio.isChecked() ? "Male" : "Female";
                String password = passwordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                if (validateInputs(email, name, surname, username, age, password, confirmPassword)) {
                    registerUser(email, name, surname, username, age, gender, password);
                }
            }
        });
    }


    // Validates the input fields to ensure that all fields are filled and the passwords match
    private boolean validateInputs(String email, String name, String surname, String username, String age, String password, String confirmPassword) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || TextUtils.isEmpty(surname) ||
                TextUtils.isEmpty(username) || TextUtils.isEmpty(age) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    // Registers the user in Firebase Authentication with email and password, then creates a user profile in Firestore
    // containing user details such as name, surname, username, birthdate, gender, and more
    private void registerUser(String email, String name, String surname, String username, String age, String gender, String password) {
        // Firebase registration with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();


                        int ageValue = Integer.parseInt(age);
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.YEAR, -ageValue);
                        long birthdate = calendar.getTimeInMillis();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("name", name);  // Store first name
                        userData.put("surname", surname);  // Store surname
                        userData.put("username", username);
                        userData.put("birthdate", birthdate);  // Stored as a timestamp
                        userData.put("gender", gender);
                        userData.put("phoneNumber", "");  // Add an empty value for now
                        userData.put("points", 0);  // Initialize with 0 points
                        userData.put("collectedLocations", new ArrayList<>());  // Empty array initially
                        userData.put("friends", new ArrayList<>());  // Empty array initially
                        userData.put("friendRequests", new ArrayList<>());  // Empty array initially
                        userData.put("badges", new ArrayList<>());  // Empty array initially
                        userData.put("profilePictureURL", null);  // Profile picture (optional)
                        userData.put("isOnline", true);  // Set user as online by default
                        userData.put("isTyping", false);  // User not typing by default

                        db.collection("users").document(email)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show());

                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

