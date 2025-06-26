package com.example.harvesthub;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class LogoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        MaterialButton loginButton = findViewById(R.id.loginButton);
        MaterialButton signupButton = findViewById(R.id.signupButton);

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(LogoutActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(LogoutActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }
} 