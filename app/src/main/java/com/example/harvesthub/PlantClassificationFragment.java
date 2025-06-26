package com.example.harvesthub;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.harvesthub.adapters.PlantAdapter;
import com.example.harvesthub.models.Plant;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class PlantClassificationFragment extends Fragment implements PlantAdapter.OnPlantClickListener {
    private EditText searchEditText;
    private ImageButton clearSearchButton;
    private MaterialButton searchButton;
    private MaterialButton vegetablesButton;
    private MaterialButton fruitsButton;
    private MaterialButton herbsButton;
    private RecyclerView plantsRecyclerView;
    private TextView emptyMessage;
    private PlantAdapter plantAdapter;
    private List<Plant> allPlants;
    private List<Plant> filteredPlants;
    private String currentCategory = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_classification, container, false);
        
        // Initialize views
        searchEditText = view.findViewById(R.id.searchEditText);
        clearSearchButton = view.findViewById(R.id.clearSearchButton);
        searchButton = view.findViewById(R.id.searchButton);
        vegetablesButton = view.findViewById(R.id.vegetablesButton);
        fruitsButton = view.findViewById(R.id.fruitsButton);
        herbsButton = view.findViewById(R.id.herbsButton);
        plantsRecyclerView = view.findViewById(R.id.plantsRecyclerView);
        emptyMessage = view.findViewById(R.id.emptyMessage);

        // Initialize data
        allPlants = new ArrayList<>();
        filteredPlants = new ArrayList<>();
        
        // Setup RecyclerView
        plantAdapter = new PlantAdapter(filteredPlants, this);
        plantsRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        plantsRecyclerView.setAdapter(plantAdapter);

        // Setup click listeners
        setupClickListeners();
        
        // Setup search functionality
        setupSearchFunctionality();

        return view;
    }

    private void setupClickListeners() {
        // Category buttons
        vegetablesButton.setOnClickListener(v -> {
            selectCategoryButton(vegetablesButton);
            loadVegetables();
        });

        fruitsButton.setOnClickListener(v -> {
            selectCategoryButton(fruitsButton);
            loadFruits();
        });

        herbsButton.setOnClickListener(v -> {
            selectCategoryButton(herbsButton);
            loadHerbs();
        });

        // Search button
        searchButton.setOnClickListener(v -> performSearch());

        // Clear search button
        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            clearSearchButton.setVisibility(View.GONE);
            filterPlantsBySearch("");
        });
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                filterPlantsBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void selectCategoryButton(MaterialButton selected) {
        MaterialButton[] buttons = {vegetablesButton, fruitsButton, herbsButton};
        for (MaterialButton btn : buttons) {
            if (btn == selected) {
                btn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.darkest_green));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                btn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.rounded_background_white));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkest_green));
            }
        }
    }

    private void performSearch() {
        String searchQuery = searchEditText.getText().toString().trim().toLowerCase();
        if (!searchQuery.isEmpty()) {
            // Check if the search query is a category
            if (searchQuery.equals("vegetables") || searchQuery.equals("vegetable")) {
                currentCategory = "Vegetable";
                loadVegetables();
                Toast.makeText(getContext(), "Showing all Vegetables", Toast.LENGTH_SHORT).show();
            } else if (searchQuery.equals("fruits") || searchQuery.equals("fruit")) {
                currentCategory = "Fruit";
                loadFruits();
                Toast.makeText(getContext(), "Showing all Fruits", Toast.LENGTH_SHORT).show();
            } else if (searchQuery.equals("herbs") || searchQuery.equals("herb")) {
                currentCategory = "Herb";
                loadHerbs();
                Toast.makeText(getContext(), "Showing all Herbs", Toast.LENGTH_SHORT).show();
            } else {
                // Search for specific plant across all categories
                searchForPlantAcrossCategories(searchQuery);
            }
        }
    }

    private void searchForPlantAcrossCategories(String query) {
        // First, check if the plant exists in vegetables
        if (searchInVegetables(query)) {
            currentCategory = "Vegetable";
            loadVegetables();
            filterPlantsBySearch(query);
            Toast.makeText(getContext(), "Found in Vegetables", Toast.LENGTH_SHORT).show();
        }
        // Then check fruits
        else if (searchInFruits(query)) {
            currentCategory = "Fruit";
            loadFruits();
            filterPlantsBySearch(query);
            Toast.makeText(getContext(), "Found in Fruits", Toast.LENGTH_SHORT).show();
        }
        // Then check herbs
        else if (searchInHerbs(query)) {
            currentCategory = "Herb";
            loadHerbs();
            filterPlantsBySearch(query);
            Toast.makeText(getContext(), "Found in Herbs", Toast.LENGTH_SHORT).show();
        }
        // If not found in any category, search in current category
        else {
            filterPlantsBySearch(query);
            if (filteredPlants.isEmpty()) {
                Toast.makeText(getContext(), "Plant not found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean searchInVegetables(String query) {
        String[] vegetableNames = {
            "kangkong", "talong", "ampalaya", "sitaw", "kalabasa", "okra", "malunggay", 
            "pechay", "kamote tops", "labanos", "upo", "patola", "alugbati", "mustasa", 
            "gabi", "tanglad", "sibuyas", "bawang", "luya", "siling labuyo"
        };
        
        for (String name : vegetableNames) {
            if (name.contains(query) || query.contains(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean searchInFruits(String query) {
        String[] fruitNames = {
            "mango", "banana", "papaya", "pineapple", "lanzones", "rambutan", "durian", 
            "atis", "guava", "soursop", "jackfruit", "santol", "calamansi", "chico", 
            "duhat", "macopa", "kamias", "balimbing", "bignay", "bayabas"
        };
        
        for (String name : fruitNames) {
            if (name.contains(query) || query.contains(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean searchInHerbs(String query) {
        String[] herbNames = {
            "tanglad", "oregano", "lagundi", "sambong", "yerba buena", "tsaang gubat", 
            "akapulko", "guava leaves", "ginger", "makabuhay"
        };
        
        for (String name : herbNames) {
            if (name.contains(query) || query.contains(name)) {
                return true;
            }
        }
        return false;
    }

    private void filterPlantsBySearch(String query) {
        if (query.isEmpty()) {
            filteredPlants.clear();
            filteredPlants.addAll(allPlants);
        } else {
            filteredPlants.clear();
            for (Plant plant : allPlants) {
                if (plant.getName().toLowerCase().contains(query.toLowerCase()) ||
                    plant.getScientificName().toLowerCase().contains(query.toLowerCase()) ||
                    plant.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                    plant.getType().toLowerCase().contains(query.toLowerCase()) ||
                    plant.getCategory().toLowerCase().contains(query.toLowerCase())) {
                    filteredPlants.add(plant);
                }
            }
        }
        plantAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredPlants.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            plantsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            plantsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadVegetables() {
        allPlants.clear();
        
        // Add all the Filipino vegetables
        allPlants.add(new Plant("Kangkong", "Ipomoea aquatica", "Native (Water Spinach)", "Vegetable", 
            "Fast-growing leafy green used in adobo or sinigang.", "Year-round", "Moist soil; full sun."));
        
        allPlants.add(new Plant("Talong", "Solanum melongena", "Talong na Mahaba (Long variety)", "Vegetable", 
            "Purple eggplant; often grilled or sautéed.", "Warm seasons", "Full sun; regular watering."));
        
        allPlants.add(new Plant("Ampalaya", "Momordica charantia", "Ilocos variety (longer, less bitter)", "Vegetable", 
            "Bitter gourd with health benefits.", "Warm and humid", "Trellis; full sun."));
        
        allPlants.add(new Plant("Sitaw", "Vigna unguiculata", "Pole Sitaw (Long type)", "Vegetable", 
            "Long beans used in pinakbet.", "Year-round", "Trellis; sunlight."));
        
        allPlants.add(new Plant("Kalabasa", "Cucurbita moschata", "Native Kalabasa (round with thick rind)", "Vegetable", 
            "Sweet squash for stews.", "Dry season", "Sunlight; wide spacing."));
        
        allPlants.add(new Plant("Okra", "Abelmoschus esculentus", "Smooth Pod Variety", "Vegetable", 
            "Mucilaginous pod used in soups.", "Summer", "Sunlight; rich soil."));
        
        allPlants.add(new Plant("Malunggay", "Moringa oleifera", "Tree-type Malunggay", "Vegetable", 
            "Leafy green rich in nutrients.", "Year-round", "Minimal; drought-tolerant."));
        
        allPlants.add(new Plant("Pechay", "Brassica rapa chinensis", "Pechay Tagalog (open-headed)", "Vegetable", 
            "Filipino bok choy.", "Cool season", "Moist soil."));
        
        allPlants.add(new Plant("Kamote Tops", "Ipomoea batatas", "Violet Kamote (root & tops used)", "Vegetable", 
            "Edible leaves of sweet potato.", "Year-round", "Minimal."));
        
        allPlants.add(new Plant("Labanos", "Raphanus sativus", "White Labanos", "Vegetable", 
            "Radish used in sinigang.", "Cool months", "Regular watering."));
        
        allPlants.add(new Plant("Upo", "Lagenaria siceraria", "Smooth-skinned Upo", "Vegetable", 
            "Gourd used in sautéed dishes.", "Warm months", "Trellis."));
        
        allPlants.add(new Plant("Patola", "Luffa cylindrica", "Ridged Patola", "Vegetable", 
            "Loofah gourd with rough skin.", "Warm season", "Support vine."));
        
        allPlants.add(new Plant("Alugbati", "Basella alba", "Red-Stemmed Alugbati", "Vegetable", 
            "Leafy vine used in sautéed dishes.", "Year-round", "Climbing support."));
        
        allPlants.add(new Plant("Mustasa", "Brassica juncea", "Native Mustard Green", "Vegetable", 
            "Slightly spicy greens.", "Cool months", "Moist soil."));
        
        allPlants.add(new Plant("Gabi", "Colocasia esculenta", "Tagalog Gabi (big leaves)", "Vegetable", 
            "Used in laing and sinigang.", "Wet season", "Moist soil."));
        
        allPlants.add(new Plant("Tanglad", "Cymbopogon citratus", "Lemongrass Local Variety", "Vegetable (Herb)", 
            "Aromatic herb for soups.", "Year-round", "Full sun."));
        
        allPlants.add(new Plant("Sibuyas", "Allium cepa", "Red Onion (Ilocos or Nueva Ecija type)", "Vegetable", 
            "Staple aromatic in Filipino cuisine.", "Dry season", "Sun; rich soil."));
        
        allPlants.add(new Plant("Bawang", "Allium sativum", "Ilocos Native Garlic", "Vegetable", 
            "Strong-flavored garlic.", "Dry months", "Full sun."));
        
        allPlants.add(new Plant("Luya", "Zingiber officinale", "Yellow Ginger", "Vegetable", 
            "Aromatic root used in cooking.", "Wet season", "Shade; moist soil."));
        
        allPlants.add(new Plant("Siling Labuyo", "Capsicum frutescens", "Native Wild Chili", "Vegetable", 
            "Small, hot chili.", "Warm months", "Sunny area."));

        filteredPlants.clear();
        filteredPlants.addAll(allPlants);
        plantAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void loadFruits() {
        allPlants.clear();
        
        // Add all the Filipino fruits
        allPlants.add(new Plant("Mango", "Mangifera indica", "Carabao Mango", "Fruit", 
            "Sweet, juicy mango.", "March–June", "Full sun."));
        
        allPlants.add(new Plant("Banana", "Musa spp.", "Saba (cooking); Lakatan (dessert)", "Fruit", 
            "Eaten ripe or cooked.", "Year-round", "Moist soil."));
        
        allPlants.add(new Plant("Papaya", "Carica papaya", "Solo Variety", "Fruit", 
            "Green when unripe; sweet when ripe.", "Year-round", "Full sun."));
        
        allPlants.add(new Plant("Pineapple", "Ananas comosus", "Queen Variety (small, sweet)", "Fruit", 
            "Spiky tropical fruit.", "Year-round", "Sun; sandy soil."));
        
        allPlants.add(new Plant("Lanzones", "Lansium domesticum", "Camiguin Lanzones", "Fruit", 
            "Sweet, segmented flesh.", "Sep–Nov", "Humid climate."));
        
        allPlants.add(new Plant("Rambutan", "Nephelium lappaceum", "Marupit Rambutan", "Fruit", 
            "Hairy red fruit.", "Jul–Oct", "Warm and wet."));
        
        allPlants.add(new Plant("Durian", "Durio zibethinus", "Puyat Variety", "Fruit", 
            "Strong aroma, creamy pulp.", "Aug–Nov", "Warm climate."));
        
        allPlants.add(new Plant("Atis", "Annona squamosa", "Sugar Apple", "Fruit", 
            "Sweet, soft, white pulp.", "Apr–Jul", "Sandy soil."));
        
        allPlants.add(new Plant("Guava", "Psidium guajava", "Bayabas Puti (white-flesh)", "Fruit", 
            "Aromatic with edible seeds.", "Aug–Oct", "Sun; drought-resistant."));
        
        allPlants.add(new Plant("Soursop", "Annona muricata", "Guyabano", "Fruit", 
            "Tangy, fibrous pulp.", "Year-round", "Moist soil."));
        
        allPlants.add(new Plant("Jackfruit", "Artocarpus heterophyllus", "Langka", "Fruit", 
            "Sweet bulbs, also cooked green.", "Mar–Aug", "Space and sun."));
        
        allPlants.add(new Plant("Santol", "Sandoricum koetjape", "Bangkok Santol", "Fruit", 
            "Sweet-sour pulp.", "Jul–Sep", "Full sun."));
        
        allPlants.add(new Plant("Calamansi", "Citrus microcarpa", "Native Calamondin", "Fruit", 
            "Sour small citrus.", "Year-round", "Regular watering."));
        
        allPlants.add(new Plant("Chico", "Manilkara zapota", "Pineras Chico", "Fruit", 
            "Brown-skinned, sweet flesh.", "Year-round", "Warm, sunny."));
        
        allPlants.add(new Plant("Duhat", "Syzygium cumini", "Lomboy", "Fruit", 
            "Purple, tangy berries.", "May–Jul", "Low maintenance."));
        
        allPlants.add(new Plant("Macopa", "Syzygium samarangense", "Pink Macopa", "Fruit", 
            "Crisp and juicy.", "Mar–May", "Humid areas."));
        
        allPlants.add(new Plant("Kamias", "Averrhoa bilimbi", "Native Kamias", "Fruit", 
            "Very sour; used in dishes.", "Year-round", "Moist environment."));
        
        allPlants.add(new Plant("Balimbing", "Averrhoa carambola", "Starfruit Sweet Variety", "Fruit", 
            "Sweet-sour star-shaped fruit.", "Year-round", "Full sun."));
        
        allPlants.add(new Plant("Bignay", "Antidesma bunius", "Red Bignay", "Fruit", 
            "Tart, used for wine.", "May–Jul", "Full sun."));
        
        allPlants.add(new Plant("Bayabas", "Psidium guajava", "Pink-Flesh Guava", "Fruit", 
            "Common backyard fruit.", "Year-round", "Drought-tolerant."));

        filteredPlants.clear();
        filteredPlants.addAll(allPlants);
        plantAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void loadHerbs() {
        allPlants.clear();
        
        // Add all the Filipino herbs
        allPlants.add(new Plant("Tanglad", "Cymbopogon citratus", "Lemongrass", "Herb", 
            "Aromatic grass commonly used in cooking and herbal tea.", "Year-round", "Full sun, well-drained soil, moderate watering."));
        
        allPlants.add(new Plant("Oregano", "Origanum vulgare", "Common Oregano", "Herb", 
            "Fragrant herb used for flavoring and treating colds/cough.", "Year-round", "Full sun, well-drained soil, occasional pruning."));
        
        allPlants.add(new Plant("Lagundi", "Vitex negundo", "Five-leaved Chaste Tree", "Herb", 
            "Medicinal shrub used to treat coughs and asthma.", "Year-round", "Full sun, minimal care, can grow into a shrub."));
        
        allPlants.add(new Plant("Sambong", "Blumea balsamifera", "Ngai Camphor", "Herb", 
            "Used for kidney health and as a diuretic.", "Year-round", "Sunny location, moderate watering."));
        
        allPlants.add(new Plant("Yerba Buena", "Clinopodium douglasii", "Douglas' Savory", "Herb", 
            "Mint-like plant used to relieve pain and headaches.", "Year-round", "Partial shade, moist soil."));
        
        allPlants.add(new Plant("Tsaang Gubat", "Ehretia microphylla", "Wild Tea", "Herb", 
            "Herbal tea plant for stomach and oral health.", "Year-round", "Full to partial sunlight, moderate watering."));
        
        allPlants.add(new Plant("Akapulko", "Cassia alata", "Ringworm Bush", "Herb", 
            "Used to treat fungal skin infections like ringworm.", "Year-round", "Full sun, regular watering."));
        
        allPlants.add(new Plant("Guava Leaves", "Psidium guajava", "Guava Tree Leaves", "Herb", 
            "Leaves used as antiseptic wash and for diarrhea.", "Year-round", "Full sun, drought-resistant."));
        
        allPlants.add(new Plant("Ginger", "Zingiber officinale", "Common Ginger", "Herb", 
            "Used in cooking and to treat nausea, colds, and cough.", "Rainy season", "Partial shade, moist and rich soil."));
        
        allPlants.add(new Plant("Makabuhay", "Tinospora crispa", "Heart-leaved Moonseed", "Herb", 
            "A vine used for treating fever, infections, and general wellness.", "Rainy season", "Moist soil, support for climbing."));

        filteredPlants.clear();
        filteredPlants.addAll(allPlants);
        plantAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    @Override
    public void onPlantClick(Plant plant) {
        // Show detailed plant information dialog
        PlantDetailsDialogFragment dialog = PlantDetailsDialogFragment.newInstance(plant);
        dialog.show(getChildFragmentManager(), "PlantDetailsDialogFragment");
    }
} 