package com.example.harvesthub;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.text.DecimalFormat;
import com.github.mikephil.charting.components.Legend;
import android.graphics.Color;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import java.util.Random;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;

public class DashboardFragment extends Fragment {
    private LineChart growthProgressChart, yieldTrendChart;
    private BarChart scheduleChart;
    private TableLayout plantDetailsTable;
    private String currentUserId;
    private DatabaseReference cropsRef, remindersRef, activityLogsRef, goalsRef;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        growthProgressChart = view.findViewById(R.id.growthProgressChart);
        yieldTrendChart = view.findViewById(R.id.yieldTrendChart);
        scheduleChart = view.findViewById(R.id.scheduleChart);
        plantDetailsTable = view.findViewById(R.id.plantDetailsTable);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cropsRef = FirebaseDatabase.getInstance().getReference("crops").child(currentUserId);
        remindersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("reminders");
        activityLogsRef = FirebaseDatabase.getInstance().getReference("activity_logs").child(currentUserId);
        goalsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("goals");

        fetchWeather();
        fetchAndDisplayPlantData();
        setupGrowthProgressChart();
        setupScheduleChart();
        setupScheduleSummary(view);
        setupYieldTrendChart();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize views and set up data here
        // TODO: Implement graph visualization
        // TODO: Load plant data and display age information
        // TODO: Set up activity timeline
    }

    private void fetchAndDisplayPlantData() {
        cropsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String[]> plantRows = new ArrayList<>();
                long now = System.currentTimeMillis();
                for (DataSnapshot cropSnap : snapshot.getChildren()) {
                    String name = cropSnap.child("name").getValue(String.class);
                    Long datePlanted = cropSnap.child("datePlanted").getValue(Long.class);
                    String details = cropSnap.child("yieldUnit").getValue(String.class);
                    String plantedDateStr = datePlanted != null ? dateFormat.format(new Date(datePlanted)) : "-";
                    String age = datePlanted != null ? String.valueOf((now - datePlanted) / (1000 * 60 * 60 * 24)) : "-";
                    plantRows.add(new String[]{name, plantedDateStr, age + " days", details});
                }
                updatePlantDetailsTable(plantRows);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePlantDetailsTable(List<String[]> data) {
        plantDetailsTable.removeAllViews();
        TableRow header = new TableRow(getContext());
        String[] headers = {"Crop", "Date Planted", "Age", "Details"};
        for (String h : headers) {
            TextView tv = new TextView(getContext());
            tv.setText(h);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextColor(android.graphics.Color.BLACK);
            header.addView(tv);
        }
        plantDetailsTable.addView(header);

        for (String[] row : data) {
            TableRow tr = new TableRow(getContext());
            for (String cell : row) {
                TextView tv = new TextView(getContext());
                tv.setText(cell);
                tv.setTextColor(android.graphics.Color.BLACK);
                tr.addView(tv);
            }
            plantDetailsTable.addView(tr);
        }
    }

    private void setupGrowthProgressChart() {
        cropsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LineDataSet> dataSets = new ArrayList<>();
                Random rand = new Random();
                int[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN};
                for (DataSnapshot cropSnap : snapshot.getChildren()) {
                    String cropName = cropSnap.child("name").getValue(String.class);
                    Long datePlanted = cropSnap.child("datePlanted").getValue(Long.class);
                    if (datePlanted == null) continue;
                    List<Entry> entries = new ArrayList<>();
                    long now = System.currentTimeMillis();
                    long daysOld = (now - datePlanted) / (1000 * 60 * 60 * 24);
                    // Simulate growth: height = 10 + 2*daysOld (for demo)
                    for (int i = 0; i <= daysOld; i += Math.max(1, daysOld/10)) {
                        entries.add(new Entry(i, 10 + 2 * i));
                    }
                    LineDataSet dataSet = new LineDataSet(entries, cropName);
                    int color = colors[rand.nextInt(colors.length)];
                    dataSet.setColor(color);
                    dataSet.setCircleColor(color);
                    dataSets.add(dataSet);
                }
                if (!dataSets.isEmpty()) {
                    List<ILineDataSet> iLineDataSets = new ArrayList<>(dataSets);
                    growthProgressChart.setData(new LineData(iLineDataSets));
                    growthProgressChart.invalidate();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupScheduleChart() {
        remindersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<BarEntry> fertEntries = new ArrayList<>();
                List<BarEntry> harvestEntries = new ArrayList<>();
                List<String> fertLabels = new ArrayList<>();
                List<String> harvestLabels = new ArrayList<>();
                int fertIndex = 0, harvestIndex = 0;
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                long now = System.currentTimeMillis();
                for (DataSnapshot reminderSnap : snapshot.getChildren()) {
                    String type = reminderSnap.child("activityType").getValue(String.class);
                    Long dateMillis = reminderSnap.child("dateMillis").getValue(Long.class);
                    if (type == null || dateMillis == null) continue;
                    String typeLower = type.toLowerCase();
                    if (typeLower.contains("fertiliz")) {
                        fertEntries.add(new BarEntry(fertIndex, 1));
                        fertLabels.add("Fertilize\n" + sdf.format(new Date(dateMillis)));
                        fertIndex++;
                    } else if (typeLower.contains("harvest")) {
                        harvestEntries.add(new BarEntry(harvestIndex, 1));
                        harvestLabels.add("Harvest\n" + sdf.format(new Date(dateMillis)));
                        harvestIndex++;
                    }
                }
                BarDataSet fertSet = new BarDataSet(fertEntries, "Fertilization");
                fertSet.setColor(Color.parseColor("#81C784"));
                fertSet.setValueTextSize(10f);
                fertSet.setValueTextColor(Color.BLACK);
                fertSet.setDrawValues(true);
                fertSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getBarLabel(BarEntry barEntry) {
                        int i = (int) barEntry.getX();
                        return i >= 0 && i < fertLabels.size() ? fertLabels.get(i) : "";
                    }
                });
                BarDataSet harvestSet = new BarDataSet(harvestEntries, "Harvest");
                harvestSet.setColor(Color.parseColor("#FFD54F"));
                harvestSet.setValueTextSize(10f);
                harvestSet.setValueTextColor(Color.BLACK);
                harvestSet.setDrawValues(true);
                harvestSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getBarLabel(BarEntry barEntry) {
                        int i = (int) barEntry.getX();
                        return i >= 0 && i < harvestLabels.size() ? harvestLabels.get(i) : "";
                    }
                });
                BarData data = new BarData(fertSet, harvestSet);
                scheduleChart.setData(data);
                scheduleChart.getDescription().setText("Upcoming and recent fertilization/harvest tasks");
                scheduleChart.getDescription().setTextSize(12f);
                scheduleChart.getLegend().setTextSize(12f);
                scheduleChart.getLegend().setTextColor(Color.BLACK);
                scheduleChart.getXAxis().setDrawLabels(false); // Hide X-axis numbers
                scheduleChart.invalidate();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupScheduleSummary(View view) {
        TextView nextFert = view.findViewById(R.id.nextFertilizationText);
        TextView lastFert = view.findViewById(R.id.lastFertilizationText);
        TextView nextHarvest = view.findViewById(R.id.nextHarvestText);
        TextView lastHarvest = view.findViewById(R.id.lastHarvestText);
        TextView expectedHarvest = view.findViewById(R.id.expectedHarvestText);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        long now = System.currentTimeMillis();

        // Next Fertilization & Next Harvest (Reminders)
        remindersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long soonestFert = null, soonestHarvest = null;
                for (DataSnapshot rem : snapshot.getChildren()) {
                    String type = rem.child("activityType").getValue(String.class);
                    Long dateMillis = rem.child("dateMillis").getValue(Long.class);
                    if (type == null || dateMillis == null || dateMillis < now) continue;
                    String typeLower = type.toLowerCase();
                    if (typeLower.contains("fertiliz")) {
                        if (soonestFert == null || dateMillis < soonestFert) soonestFert = dateMillis;
                    } else if (typeLower.contains("harvest")) {
                        if (soonestHarvest == null || dateMillis < soonestHarvest) soonestHarvest = dateMillis;
                    }
                }
                nextFert.setText("Next Fertilization: " + (soonestFert != null ? sdf.format(new Date(soonestFert)) : "-"));
                nextHarvest.setText("Next Harvest: " + (soonestHarvest != null ? sdf.format(new Date(soonestHarvest)) : "-"));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Last Fertilization & Last Harvest (Log Activity)
        activityLogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long lastFertMillis = null, lastHarvestMillis = null;
                for (DataSnapshot log : snapshot.getChildren()) {
                    String type = log.child("activityType").getValue(String.class);
                    String dateStr = log.child("date").getValue(String.class);
                    if (type == null || dateStr == null) continue;
                    String typeLower = type.toLowerCase();
                    try {
                        Date d = dateFormat.parse(dateStr);
                        long millis = d != null ? d.getTime() : 0;
                        if (typeLower.contains("fertiliz")) {
                            if (lastFertMillis == null || millis > lastFertMillis) lastFertMillis = millis;
                        } else if (typeLower.contains("harvest")) {
                            if (lastHarvestMillis == null || millis > lastHarvestMillis) lastHarvestMillis = millis;
                        }
                    } catch (Exception ignored) {}
                }
                lastFert.setText("Last Fertilization: " + (lastFertMillis != null ? sdf.format(new Date(lastFertMillis)) : "-"));
                lastHarvest.setText("Last Harvest: " + (lastHarvestMillis != null ? sdf.format(new Date(lastHarvestMillis)) : "-"));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Expected Harvest (Goals)
        goalsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long soonestExpected = null;
                for (DataSnapshot goal : snapshot.getChildren()) {
                    String type = goal.child("type").getValue(String.class);
                    String dateStr = goal.child("targetDate").getValue(String.class);
                    if (type == null || dateStr == null) continue;
                    String typeLower = type.toLowerCase();
                    if (typeLower.contains("harvest")) {
                        try {
                            Date d = dateFormat.parse(dateStr);
                            long millis = d != null ? d.getTime() : 0;
                            if (millis > now && (soonestExpected == null || millis < soonestExpected)) soonestExpected = millis;
                        } catch (Exception ignored) {}
                    }
                }
                expectedHarvest.setText("Expected Harvest: " + (soonestExpected != null ? sdf.format(new Date(soonestExpected)) : "-"));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupYieldTrendChart() {
        activityLogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Float> yieldByDate = new HashMap<>();
                ArrayList<String> dateLabels = new ArrayList<>();
                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                TextView yieldTrendDateLabel = getView() != null ? getView().findViewById(R.id.yieldTrendDateLabel) : null;
                for (DataSnapshot logSnap : snapshot.getChildren()) {
                    String type = logSnap.child("activityType").getValue(String.class);
                    if (type == null) continue;
                    String typeLower = type.toLowerCase();
                    if (!typeLower.contains("harvest")) continue;
                    String dateStr = logSnap.child("date").getValue(String.class);
                    String yieldAmountStr = logSnap.child("yieldAmount").getValue(String.class);
                    String description = logSnap.child("description").getValue(String.class);
                    float yield = 1f;
                    if (yieldAmountStr != null && !yieldAmountStr.isEmpty()) {
                        try {
                            yield = Float.parseFloat(yieldAmountStr);
                        } catch (Exception ignored) {}
                    } else if (description != null) {
                        try {
                            yield = Float.parseFloat(description.replaceAll("[^0-9.]", ""));
                        } catch (Exception ignored) {}
                    }
                    String label = null;
                    if (dateStr != null && !dateStr.isEmpty()) {
                        try {
                            Date date = dateFormat.parse(dateStr);
                            String dayKey = dayFormat.format(date);
                            label = displayFormat.format(date);
                            if (!yieldByDate.containsKey(dayKey)) {
                                dateLabels.add(label);
                            }
                            yieldByDate.put(dayKey, yieldByDate.getOrDefault(dayKey, 0f) + yield);
                        } catch (Exception e) {
                            // If date parsing fails, fallback to 'Unknown Date'
                            label = "Unknown Date";
                            if (!yieldByDate.containsKey(label)) {
                                dateLabels.add(label);
                            }
                            yieldByDate.put(label, yieldByDate.getOrDefault(label, 0f) + yield);
                        }
                    } else {
                        // If date is missing, use 'Unknown Date' as label
                        label = "Unknown Date";
                        if (!yieldByDate.containsKey(label)) {
                            dateLabels.add(label);
                        }
                        yieldByDate.put(label, yieldByDate.getOrDefault(label, 0f) + yield);
                    }
                }
                List<Entry> entries = new ArrayList<>();
                int idx = 0;
                for (String dayKey : yieldByDate.keySet()) {
                    entries.add(new Entry(idx++, yieldByDate.get(dayKey)));
                }
                LineDataSet set = new LineDataSet(entries, "Yield (kg)");
                set.setColor(Color.parseColor("#29B6F6"));
                set.setCircleColor(Color.parseColor("#29B6F6"));
                set.setValueTextSize(10f);
                set.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getPointLabel(Entry entry) {
                        return String.format(Locale.getDefault(), "%.0f kg", entry.getY());
                    }
                });
                LineData lineData = new LineData(set);
                yieldTrendChart.setData(lineData);
                // Set X-axis labels to dates
                yieldTrendChart.getXAxis().setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                        int i = (int) value;
                        if (i >= 0 && i < dateLabels.size()) return dateLabels.get(i);
                        return "";
                    }
                });
                yieldTrendChart.getXAxis().setGranularity(1f);
                yieldTrendChart.getXAxis().setGranularityEnabled(true);
                yieldTrendChart.getXAxis().setLabelRotationAngle(-45f);
                if (dateLabels.size() == 1) {
                    yieldTrendChart.getXAxis().setLabelCount(0, false); // Hide X-axis label
                    yieldTrendChart.getXAxis().setDrawLabels(false);
                    if (yieldTrendDateLabel != null) {
                        yieldTrendDateLabel.setText(dateLabels.get(0));
                        yieldTrendDateLabel.setVisibility(View.VISIBLE);
                    }
                } else {
                    yieldTrendChart.getXAxis().setLabelCount(dateLabels.size(), false);
                    yieldTrendChart.getXAxis().setTextSize(10f);
                    yieldTrendChart.getXAxis().setTextColor(Color.BLACK);
                    yieldTrendChart.getXAxis().setDrawLabels(true);
                    yieldTrendChart.getXAxis().setAvoidFirstLastClipping(true);
                    if (yieldTrendDateLabel != null) {
                        yieldTrendDateLabel.setVisibility(View.GONE);
                    }
                }
                yieldTrendChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                        return String.format(Locale.getDefault(), "%.0f kg", value);
                    }
                });
                yieldTrendChart.getDescription().setText("Total yield harvested per day");
                yieldTrendChart.getDescription().setTextSize(12f);
                yieldTrendChart.invalidate();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchWeather() {
        // Manila City coordinates
        String apiKey = "7b883cb84a3c060d93a6fff09524f991"; // <-- Replace with your actual API key
        String urlString = "https://api.openweathermap.org/data/2.5/weather?q=Manila&units=metric&appid=" + apiKey;

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
                if (json != null && getView() != null) {
                    try {
                        double temp = json.getJSONObject("main").getDouble("temp");
                        String desc = json.getJSONArray("weather").getJSONObject(0).getString("description");
                        String icon = json.getJSONArray("weather").getJSONObject(0).getString("icon");
                        String location = json.getString("name");

                        TextView weatherTemp = getView().findViewById(R.id.weatherTemp);
                        TextView weatherDesc = getView().findViewById(R.id.weatherDesc);
                        TextView weatherLocation = getView().findViewById(R.id.weatherLocation);
                        ImageView weatherIcon = getView().findViewById(R.id.weatherIcon);

                        weatherTemp.setText(String.format("%.0f°C", temp));
                        weatherDesc.setText(desc.substring(0,1).toUpperCase() + desc.substring(1));
                        weatherLocation.setText(location);

                        String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
                        Picasso.get().load(iconUrl).into(weatherIcon);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }
}
