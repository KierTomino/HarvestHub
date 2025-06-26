package com.example.harvesthub;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.ImageView;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkNotificationPermission();

        ImageView toolbarLogo = findViewById(R.id.toolbarLogo);
        toolbarLogo.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
        });

        // Set up navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Update username and avatar in navigation drawer header
        View headerView = navigationView.getHeaderView(0);
        TextView userNameTextView = headerView.findViewById(R.id.textView);
        ImageView navHeaderAvatar = headerView.findViewById(R.id.navHeaderAvatar);
        SharedPreferences prefs = getSharedPreferences("HarvestHubPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "User"); // Default to "User" if not found
        userNameTextView.setText(username);

        int selectedAvatar = prefs.getInt("avatar", 0); // 0 = none, 1 = boy, 2 = girl
        if (selectedAvatar == 1) {
            navHeaderAvatar.setImageResource(R.drawable.boy);
        } else if (selectedAvatar == 2) {
            navHeaderAvatar.setImageResource(R.drawable.girl);
        } else {
            navHeaderAvatar.setImageResource(R.drawable.ic_camera); // fallback icon
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            // Load the default fragment (Homepage) when the activity is first created
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_homepage);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_homepage) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_log_activity) {
            selectedFragment = new LogActivityFragment();
        } else if (itemId == R.id.nav_set_goal) {
            selectedFragment = new SetGoalsFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else if (itemId == R.id.nav_reminders) {
            selectedFragment = new RemindersFragment();
        } else if (itemId == R.id.nav_yield_tracker) {
            selectedFragment = new YieldTrackerFragment();
        } else if (itemId == R.id.nav_plant_classification) {
            selectedFragment = new PlantClassificationFragment();
        } else if (itemId == R.id.nav_dashboard) {
            selectedFragment = new DashboardFragment();
            // Handle Dashboard fragment
        } else if (itemId == R.id.nav_logout) {
            // Handle logout
            mAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, LogoutActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish(); // Finish HomeActivity
        }

        if (selectedFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentContainer, selectedFragment);
            fragmentTransaction.commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void navigateToLogActivity() {
        Fragment selectedFragment = new LogActivityFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, selectedFragment);
        fragmentTransaction.commit();
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted. You can proceed with notifications.
            } else {
                // Permission denied. Inform user or disable notification features.
            }
        }
    }
} 