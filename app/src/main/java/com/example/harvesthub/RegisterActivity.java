package com.example.harvesthub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import java.util.HashMap;
import java.util.Map;
import android.content.SharedPreferences;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private MaterialButton registerButton;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            
            // Enable offline persistence
            database.setPersistenceEnabled(true);

            // Initialize views
            nameInput = findViewById(R.id.nameInput);
            emailInput = findViewById(R.id.emailInput);
            passwordInput = findViewById(R.id.passwordInput);
            confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
            registerButton = findViewById(R.id.registerButton);

            // Set up click listeners
            registerButton.setOnClickListener(v -> attemptRegistration());
            
            findViewById(R.id.loginLink).setOnClickListener(v -> {
                finish(); // Go back to login screen
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void attemptRegistration() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        registerButton.setEnabled(false);
        registerButton.setText("Creating account...");

        try {
            // Create user with Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        // Update user profile with name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

                        mAuth.getCurrentUser().updateProfile(profileUpdates)
                            .addOnCompleteListener(profileTask -> {
                                if (profileTask.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");
                                    
                                    // Save full name to SharedPreferences
                                    SharedPreferences preferences = getSharedPreferences("HarvestHubPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("fullName", name);
                                    editor.apply();

                                    // Create user data in Realtime Database
                                    String userId = mAuth.getCurrentUser().getUid();
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("name", name);
                                    userData.put("email", email);
                                    userData.put("createdAt", System.currentTimeMillis());

                                    database.getReference("users").child(userId).setValue(userData)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                Log.d(TAG, "User data saved to database");
                                                // Navigate to home screen
                                                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                                finish();
                                            } else {
                                                Log.e(TAG, "Error saving user data", dbTask.getException());
                                                Toast.makeText(RegisterActivity.this, 
                                                    "Error saving user data: " + dbTask.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                                registerButton.setEnabled(true);
                                                registerButton.setText("REGISTER");
                                            }
                                        });
                                } else {
                                    Log.e(TAG, "Error updating profile", profileTask.getException());
                                    Toast.makeText(RegisterActivity.this,
                                        "Error updating profile: " + profileTask.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                    registerButton.setEnabled(true);
                                    registerButton.setText("REGISTER");
                                }
                            });
                    } else {
                        // Registration failed
                        Log.e(TAG, "createUserWithEmail:failure", task.getException());
                        String errorMessage;
                        if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                            errorMessage = "Password is too weak. Please use a stronger password.";
                        } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            errorMessage = "An account already exists with this email. Please login instead.";
                        } else {
                            errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Unknown error occurred";
                        }
                        Toast.makeText(RegisterActivity.this,
                            "Registration failed: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                        registerButton.setEnabled(true);
                        registerButton.setText("REGISTER");
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in attemptRegistration", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            registerButton.setEnabled(true);
            registerButton.setText("REGISTER");
        }
    }
} 