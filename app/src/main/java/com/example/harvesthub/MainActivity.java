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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            mAuth = FirebaseAuth.getInstance();
            
            // Check if user is already logged in
            if (mAuth.getCurrentUser() != null) {
                Log.d(TAG, "User already logged in, redirecting to HomeActivity");
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
                return;
            }
            
            setContentView(R.layout.activity_main);
            
            // Initialize views
            emailInput = findViewById(R.id.emailInput);
            passwordInput = findViewById(R.id.passwordInput);
            loginButton = findViewById(R.id.loginButton);
            
            // Set up click listeners
            loginButton.setOnClickListener(v -> attemptLogin());
            
            findViewById(R.id.registerLink).setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        try {
            // Attempt to sign in with Firebase
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithEmail:success");

                        // Save full name to SharedPreferences
                        String fullName = mAuth.getCurrentUser().getDisplayName();
                        SharedPreferences preferences = getSharedPreferences("HarvestHubPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("fullName", fullName);
                        editor.apply();

                        // Navigate to home screen
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user
                        Log.e(TAG, "signInWithEmail:failure", task.getException());
                        String errorMessage;
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            errorMessage = "No account found with this email. Please register first.";
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Invalid password. Please try again.";
                        } else {
                            errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Unknown error occurred";
                        }
                        Toast.makeText(MainActivity.this,
                            "Authentication failed: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                        loginButton.setEnabled(true);
                        loginButton.setText("LOGIN");
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in attemptLogin", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            loginButton.setEnabled(true);
            loginButton.setText("LOGIN");
        }
    }
} 