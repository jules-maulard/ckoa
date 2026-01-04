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
import com.example.ckoa.managers.GameStatsManager;
import com.example.ckoa.managers.ShapeGameManager;
import com.example.ckoa.models.Guess;

import java.util.ArrayList;
import java.util.List;

public class DailyShapeActivity extends AppCompatActivity {

    private GeoShapeView geoShapeView;
    private AutoCompleteTextView inputCountry;
    private ListView listHistory;
    private Button btnGuess;
    private ImageButton btnBack;
    private Button btnNextLevel;

    private ShapeGameManager gameManager;
    private GameStatsManager gameStatsManager;

    private ArrayAdapter<String> historyAdapter;
    private List<String> historyList;

    private int attemptsCount = 0;
    private boolean isGameFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_shape);

        geoShapeView = findViewById(R.id.geoShapeView);
        inputCountry = findViewById(R.id.inputCountry);
        listHistory = findViewById(R.id.listHistory);
        btnGuess = findViewById(R.id.btnGuess);
        btnBack = findViewById(R.id.btnBack);
        btnNextLevel = findViewById(R.id.btnNextLevel);

        gameManager = new ShapeGameManager(this);
        gameStatsManager = new GameStatsManager(this);

        historyList = new ArrayList<>();
        historyAdapter = new ArrayAdapter<>(this, R.layout.item_guess, historyList);
        listHistory.setAdapter(historyAdapter);

        loadShapeView();
        setupAutoComplete();
        loadHistory();

        btnBack.setOnClickListener(v -> finish());
        btnGuess.setOnClickListener(v -> processGuess());
        btnNextLevel.setOnClickListener(v -> {
            startActivity(new Intent(this, DailyFlagActivity.class));
            finish();
        });
    }

    private void loadShapeView() {
        String geoJson = gameManager.loadDailyTarget();
        if (geoJson != null) {
            geoShapeView.setGeoJson(geoJson);
        } else {
            Toast.makeText(this, "Game loading failed", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupAutoComplete() {
        List<String> allCountryNames = gameManager.getAllCountryNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allCountryNames);
        inputCountry.setAdapter(adapter);
    }

    private void loadHistory() {
        List<Guess> guesses = gameManager.getHistoryGuesses();

        for (Guess guess : guesses) {
            attemptsCount++;
            String countryName = gameManager.getCountryNameByIso(guess.getIso3());

            addGuessToHistory(guess, countryName);

            if (guess.getIs_correct() || attemptsCount >= 6) {
                endGame();
            }
        }
    }

    private void processGuess() {
        if (isGameFinished) return;

        String guessName = inputCountry.getText().toString().trim();
        if (guessName.isEmpty()) return;

        Guess result = gameManager.makeGuess(guessName);

        if (result == null) {
            Toast.makeText(this, "Unknown country", Toast.LENGTH_SHORT).show();
            return;
        }

        attemptsCount++;
        addGuessToHistory(result, guessName);

        if (result.getIs_correct()) {
            gameStatsManager.saveGameResult(attemptsCount, true);
            endGame();
        } else {
            if (attemptsCount >= 6) {
                gameStatsManager.saveGameResult(attemptsCount, false);
                endGame();
                Toast.makeText(this, "Lost! It was: " + gameManager.getTargetName(), Toast.LENGTH_LONG).show();
            } else {
                inputCountry.setText("");
            }
        }
    }

    private void addGuessToHistory(Guess guess, String countryName) {
        boolean isWin = guess.getIs_correct();
        String emoji = isWin ? "🏆" : "❌";

        String arrow = isWin ? "" : gameManager.getDirectionArrow(guess.getBearing_deg().floatValue());

        String historyItem = isWin
                ? String.format("%s %s - Won!", emoji, countryName)
                : String.format("%s %s : %.0f km %s", emoji, countryName, guess.getDistance_km(), arrow);

        historyList.add(0, historyItem);
        historyAdapter.notifyDataSetChanged();
    }

    private void endGame() {
        isGameFinished = true;
        btnGuess.setEnabled(false);
        inputCountry.setEnabled(false);
        btnNextLevel.setVisibility(View.VISIBLE);
    }
}