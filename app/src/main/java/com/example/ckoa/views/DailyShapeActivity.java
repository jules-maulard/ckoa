package com.example.ckoa.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ckoa.R;
import com.example.ckoa.managers.ShapeGameManager;
import com.example.ckoa.models.ShapeGuess;

import java.util.ArrayList;
import java.util.List;

public class DailyShapeActivity extends AppCompatActivity {

    private ShapeGameManager gameManager;

    private ImageButton backButton;
    private Button nextLevelButton;
    private GeoShapeView geoShapeView;
    private AutoCompleteTextView countryInput;
    private Button guessButton;
    private ListView historyListView;

    private List<String> historyList;
    private ArrayAdapter<String> historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_shape);

        gameManager = new ShapeGameManager(this);

        initializeViews();

        loadShapeView();
        setupAutoComplete();
        initializeHistoryAdapter();
        loadHistory();

        backButton.setOnClickListener(v -> finish());
        guessButton.setOnClickListener(v -> submitGuess());
        nextLevelButton.setOnClickListener(v -> startActivity(new Intent(this, DailyFlagActivity.class)));
    }

    private void initializeViews() {
        geoShapeView = findViewById(R.id.geoShapeView);
        countryInput = findViewById(R.id.inputCountry);
        historyListView = findViewById(R.id.listHistory);
        guessButton = findViewById(R.id.btnGuess);
        backButton = findViewById(R.id.btnBack);
        nextLevelButton = findViewById(R.id.btnNextLevel);
    }

    private void initializeHistoryAdapter() {
        historyList = new ArrayList<>();
        historyAdapter = new ArrayAdapter<>(this, R.layout.item_guess, historyList);
        historyListView.setAdapter(historyAdapter);
    }

    private void loadShapeView() {
        String geoJson = gameManager.loadDailyTarget();

        if (geoJson == null) {
            Toast.makeText(this, "Game loading failed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        geoShapeView.setGeoJson(geoJson);
    }

    private void setupAutoComplete() {
        List<String> allCountryNames = gameManager.getAllCountryNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allCountryNames);
        countryInput.setAdapter(adapter);
    }

    private void loadHistory() {
        gameManager.loadGameState();
        List<ShapeGuess> shapeGuesses = gameManager.getHistoryGuesses();

        for (ShapeGuess shapeGuess : shapeGuesses) {
            String countryName = gameManager.getCountryNameByIso(shapeGuess.getIso3());
            addGuessToUI(shapeGuess, countryName);
        }

        if (gameManager.isGameFinished()) {
            disableGameInteractions();
        }
    }

    private void submitGuess() {
        String guessName = countryInput.getText().toString().trim();
        if (guessName.isEmpty()) return;

        ShapeGuess result = gameManager.processUserGuess(guessName);

        if (result == null) {
            Toast.makeText(this, "Unknown country", Toast.LENGTH_SHORT).show();
            return;
        }

        addGuessToUI(result, guessName);
        countryInput.setText("");

        if (gameManager.isGameFinished()) {
            disableGameInteractions();
            if (gameManager.isGameLost()) {
                Toast.makeText(this, "Lost! It was: " + gameManager.getTargetName(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addGuessToUI(ShapeGuess shapeGuess, String countryName) {
        String historyItem = gameManager.formatHistoryItem(shapeGuess, countryName);
        historyList.add(0, historyItem);
        historyAdapter.notifyDataSetChanged();
    }

    private void disableGameInteractions() {
        guessButton.setEnabled(false);
        countryInput.setEnabled(false);
        nextLevelButton.setVisibility(View.VISIBLE);
    }
}