package com.example.harvesthub;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.harvesthub.adapters.RecentActivityAdapter;
import com.example.harvesthub.models.Activity;
import com.example.harvesthub.adapters.CropAdapter;
import com.example.harvesthub.models.Crop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONArray;
import org.json.JSONException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.graphics.Rect;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.app.AlertDialog;
import java.text.DateFormat;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import android.os.AsyncTask;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;

public class HomeFragment extends Fragment {
    private SimpleDateFormat dateFormat;
    private TextView welcomeText;
    private RecyclerView quickFarmRecyclerView;
    private CropAdapter quickFarmAdapter;
    private List<Crop> quickFarmCrops;
    private DatabaseReference cropsRef;
    private String currentUserId;
    private DatabaseReference remindersRef;
    private DatabaseReference activityLogsRef;
    private RecyclerView recentActivitiesRecyclerView;
    private RecentActivityAdapter activityAdapter;
    private List<Activity> activities;
    private Timer dateTimer;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;
    private Double lastLat = null, lastLon = null;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        welcomeText = view.findViewById(R.id.welcome_text);
        quickFarmRecyclerView = view.findViewById(R.id.quickFarmRecyclerView);

        quickFarmCrops = new ArrayList<>();
        quickFarmAdapter = new CropAdapter(quickFarmCrops, new CropAdapter.OnCropActionListener() {
            @Override
            public void onAddProgressPhoto(Crop crop) {}
            @Override
            public void onLogHarvest(Crop crop) {}
            @Override
            public void onViewDetails(Crop crop) {
                // Show crop details dialog
                CropDetailsDialogFragment dialog = CropDetailsDialogFragment.newInstance(crop);
                dialog.show(getParentFragmentManager(), "CropDetailsDialogFragment");
            }
            @Override
            public void onGotoYieldTracker(Crop crop) {
                // Navigate to Yield Tracker fragment
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new YieldTrackerFragment())
                        .addToBackStack(null)
                        .commit();
                }
            }
        }, true);
        quickFarmRecyclerView.setLayoutManager(new NonScrollableLinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        quickFarmRecyclerView.setAdapter(quickFarmAdapter);
        // Add 14dp gap only between cards
        int gap = (int) (14 * getResources().getDisplayMetrics().density); // 14dp to px
        quickFarmRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                if (position > 0) {
                    outRect.left = gap; // Add gap to the left of every card except the first
                }
            }
        });
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cropsRef = FirebaseDatabase.getInstance().getReference("crops").child(currentUserId);
        loadQuickFarmCrops();

        activityLogsRef = FirebaseDatabase.getInstance().getReference("activity_logs").child(currentUserId);
        recentActivitiesRecyclerView = view.findViewById(R.id.recent_activities_recycler_view);
        activities = new ArrayList<>();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        activityAdapter = new RecentActivityAdapter(activities);
        recentActivitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentActivitiesRecyclerView.setAdapter(activityAdapter);
        loadRecentActivities();
        updateWelcomeText();

        // Section-level arrow to Yield Tracker
        ImageView gotoYieldTrackerSectionArrow = view.findViewById(R.id.gotoYieldTrackerSectionArrow);
        if (gotoYieldTrackerSectionArrow != null) {
            gotoYieldTrackerSectionArrow.setOnClickListener(v -> {
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new YieldTrackerFragment())
                        .addToBackStack(null)
                        .commit();
                }
            });
        }

        remindersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("reminders");
        loadTodaysTasks(view);

        TextView viewAllRecentActivities = view.findViewById(R.id.viewAllRecentActivities);
        if (viewAllRecentActivities != null) {
            viewAllRecentActivities.setOnClickListener(v -> {
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new LogActivityFragment())
                        .addToBackStack(null)
                        .commit();
                }
            });
        }

        // Update date/time immediately and start timer for updates
        updateDateTime(view);
        startDateTimeUpdater(view);
        // Fetch and update weather
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestAndFetchWeather(view);

        return view;
    }

    private void loadRecentActivities() {
        activities.clear();
        activityLogsRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Activity> temp = new ArrayList<>();
                for (DataSnapshot actSnap : snapshot.getChildren()) {
                    String type = actSnap.child("activityType").getValue(String.class);
                    String description = actSnap.child("description").getValue(String.class);
                    String notes = actSnap.child("notes").getValue(String.class);
                    Long dateMillis = null;
                    if (actSnap.child("dateMillis").exists()) {
                        dateMillis = actSnap.child("dateMillis").getValue(Long.class);
                    } else if (actSnap.child("date").exists()) {
                        try {
                            String dateStr = actSnap.child("date").getValue(String.class);
                            if (dateStr != null) {
                                dateMillis = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).parse(dateStr).getTime();
                            }
                        } catch (Exception ignored) {}
                    }
                    Date date = dateMillis != null ? new Date(dateMillis) : null;
                    if (type != null && description != null && date != null) {
                        temp.add(new Activity(type, description, notes, date));
                    }
                }
                // Sort by date descending
                Collections.sort(temp, (a, b) -> b.getDate().compareTo(a.getDate()));
                if (!temp.isEmpty()) {
                    activities.add(temp.get(0)); // Only show the most recent
                }
                activityAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void updateWelcomeText() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("HarvestHubPrefs", Context.MODE_PRIVATE);
        String username = preferences.getString("username", "User");
        welcomeText.setText("Hi " + username + "!");
    }

    private void loadQuickFarmCrops() {
        cropsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Crop> readyCrops = new ArrayList<>();
                List<Crop> upcomingCrops = new ArrayList<>();
                long currentTime = System.currentTimeMillis();

                for (DataSnapshot cropSnapshot : snapshot.getChildren()) {
                    Crop crop = cropSnapshot.getValue(Crop.class);
                    if (crop != null && crop.getActualHarvestDate() == 0) {
                        crop.setId(cropSnapshot.getKey());
                        if (crop.getExpectedHarvestDate() <= currentTime) {
                            readyCrops.add(crop);
                        } else {
                            upcomingCrops.add(crop);
                        }
                    }
                }

                // Sort both lists by expectedHarvestDate ascending
                Collections.sort(readyCrops, new Comparator<Crop>() {
                    @Override
                    public int compare(Crop c1, Crop c2) {
                        return Long.compare(c1.getExpectedHarvestDate(), c2.getExpectedHarvestDate());
                    }
                });
                Collections.sort(upcomingCrops, new Comparator<Crop>() {
                    @Override
                    public int compare(Crop c1, Crop c2) {
                        return Long.compare(c1.getExpectedHarvestDate(), c2.getExpectedHarvestDate());
                    }
                });

                quickFarmCrops.clear();
                // Add up to 2 ready crops first
                for (int i = 0; i < Math.min(2, readyCrops.size()); i++) {
                    quickFarmCrops.add(readyCrops.get(i));
                }
                // If less than 2, fill with upcoming crops
                for (int i = 0; quickFarmCrops.size() < 2 && i < upcomingCrops.size(); i++) {
                    quickFarmCrops.add(upcomingCrops.get(i));
                }

                quickFarmAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void loadTodaysTasks(View rootView) {
        LinearLayout tasksContainer = rootView.findViewById(R.id.todaysTasksContainer);
        tasksContainer.removeAllViews();
        long now = System.currentTimeMillis();
        // Calculate start and end of today
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        long endOfDay = cal.getTimeInMillis();
        remindersRef.orderByChild("dateMillis").startAt(startOfDay).endAt(endOfDay)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean hasTasks = false;
                    for (DataSnapshot remSnap : snapshot.getChildren()) {
                        String status = remSnap.child("status").getValue(String.class);
                        if (status != null && status.equals("pending")) {
                            String activityType = remSnap.child("activityType").getValue(String.class);
                            String cropName = remSnap.child("cropName").getValue(String.class);
                            String taskText = (activityType != null ? activityType : "Task");
                            if (cropName != null && !cropName.isEmpty() && !cropName.equals("General")) {
                                taskText += " - " + cropName;
                            }
                            LinearLayout row = new LinearLayout(getContext());
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setGravity(Gravity.CENTER_VERTICAL);
                            row.setPadding(0, 0, 0, 8);
                            ImageView checkIcon = new ImageView(getContext());
                            checkIcon.setImageResource(R.drawable.ic_checkbox_checked);
                            checkIcon.setColorFilter(getResources().getColor(R.color.darkest_green));
                            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(48, 48);
                            checkIcon.setLayoutParams(iconParams);
                            TextView taskTextView = new TextView(getContext());
                            taskTextView.setText(taskText);
                            taskTextView.setTextColor(getResources().getColor(R.color.darkest_green));
                            taskTextView.setTextSize(16);
                            taskTextView.setPadding(16, 0, 0, 0);
                            row.addView(checkIcon);
                            row.addView(taskTextView);
                            tasksContainer.addView(row);
                            hasTasks = true;
                        }
                    }
                    if (!hasTasks) {
                        TextView noTasks = new TextView(getContext());
                        noTasks.setText("No tasks for today!");
                        noTasks.setTextColor(getResources().getColor(R.color.darkest_green));
                        noTasks.setTextSize(16);
                        noTasks.setGravity(Gravity.CENTER);
                        tasksContainer.addView(noTasks);
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDateTime(getView());
        startDateTimeUpdater(getView());
        updateWelcomeText();
        loadQuickFarmCrops();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dateTimer != null) {
            dateTimer.cancel();
            dateTimer = null;
        }
    }

    private void updateDateTime(View rootView) {
        if (rootView == null) return;
        TextView dateTimeText = rootView.findViewById(R.id.date_time_text);
        if (dateTimeText != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, MMM dd • HH:mm", java.util.Locale.getDefault());
            String now = sdf.format(new java.util.Date());
            dateTimeText.setText(now);
        }
    }

    private void startDateTimeUpdater(View rootView) {
        if (dateTimer != null) {
            dateTimer.cancel();
        }
        dateTimer = new Timer();
        dateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> updateDateTime(rootView));
                }
            }
        }, 60000, 60000); // update every minute
    }

    private void requestAndFetchWeather(View rootView) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            // Will retry in onRequestPermissionsResult
            fetchWeather(rootView, null, null); // fallback to Manila
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    lastLat = location.getLatitude();
                    lastLon = location.getLongitude();
                    fetchWeather(rootView, lastLat, lastLon);
                } else {
                    fetchWeather(rootView, null, null); // fallback
                }
            }).addOnFailureListener(e -> fetchWeather(rootView, null, null));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestAndFetchWeather(getView());
            } else {
                fetchWeather(getView(), null, null); // fallback
            }
        }
    }

    private void fetchWeather(View rootView, Double lat, Double lon) {
        String apiKey = "7b883cb84a3c060d93a6fff09524f991";
        String urlString;
        if (lat != null && lon != null) {
            urlString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + apiKey;
        } else {
            urlString = "https://api.openweathermap.org/data/2.5/weather?q=Manila&units=metric&appid=" + apiKey;
        }
        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlString);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    return new JSONObject(sb.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            protected void onPostExecute(JSONObject json) {
                if (json != null && rootView != null) {
                    try {
                        double temp = json.getJSONObject("main").getDouble("temp");
                        String desc = json.getJSONArray("weather").getJSONObject(0).getString("description");
                        String icon = json.getJSONArray("weather").getJSONObject(0).getString("icon");
                        TextView tempText = rootView.findViewById(R.id.temperature_text);
                        if (tempText != null) {
                            tempText.setText(String.format("%.0f°C", temp));
                        }
                        // Optionally update weather icon/desc here
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }
} 