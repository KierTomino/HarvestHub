package com.example.harvesthub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {
    private ImageView avatarImage;
    private TextView userNameDisplay;
    private int selectedAvatar = 0; // 0 = none, 1 = boy, 2 = girl

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        avatarImage = view.findViewById(R.id.avatarImage);
        Button saveButton = view.findViewById(R.id.saveProfile);
        Button changePasswordButton = view.findViewById(R.id.changePassword);
        userNameDisplay = view.findViewById(R.id.userNameDisplay);

        // Profile fields
        EditText fullName = view.findViewById(R.id.fullName);
        EditText username = view.findViewById(R.id.username);
        EditText email = view.findViewById(R.id.email);
        EditText phone = view.findViewById(R.id.phone);
        EditText bio = view.findViewById(R.id.bio);

        // Load saved profile
        SharedPreferences prefs = requireContext().getSharedPreferences("HarvestHubPrefs", Context.MODE_PRIVATE);
        selectedAvatar = prefs.getInt("avatar", 0);
        updateAvatarImage();
        fullName.setText(prefs.getString("fullName", ""));
        userNameDisplay.setText(prefs.getString("fullName", ""));
        username.setText(prefs.getString("username", ""));
        // Set email from Firebase Authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email.setText(user.getEmail());
            email.setEnabled(false); // Make email field non-editable
        }
        phone.setText(prefs.getString("phone", ""));
        bio.setText(prefs.getString("bio", ""));

        avatarImage.setOnClickListener(v -> showAvatarDialog());

        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("avatar", selectedAvatar);
            editor.putString("fullName", fullName.getText().toString());
            editor.putString("username", username.getText().toString());
            // Removed saving email as it's fetched from Firebase
            editor.putString("phone", phone.getText().toString());
            editor.putString("bio", bio.getText().toString());
            editor.apply();
            Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show();
            userNameDisplay.setText(fullName.getText().toString());
        });

        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

        return view;
    }

    private void showAvatarDialog() {
        String[] options = {"Boy", "Girl"};
        int[] avatarDrawables = {R.drawable.boy, R.drawable.girl};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Avatar");
        builder.setItems(options, (dialog, which) -> {
            selectedAvatar = which + 1;
            updateAvatarImage();
        });
        builder.show();
    }

    private void updateAvatarImage() {
        if (selectedAvatar == 1) {
            avatarImage.setImageResource(R.drawable.boy);
        } else if (selectedAvatar == 2) {
            avatarImage.setImageResource(R.drawable.girl);
        } else {
            avatarImage.setImageResource(R.drawable.ic_camera); // fallback icon
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Password");
        final EditText input = new EditText(requireContext());
        input.setHint("Enter new password");
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newPassword = input.getText().toString();
            // Here you would update the password in your backend or Firebase
            Toast.makeText(requireContext(), "Password changed! (Demo only)", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
} 