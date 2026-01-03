package com.example.ckoa.views;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ckoa.R;
import com.example.ckoa.managers.DatabaseHelper;
import com.example.ckoa.managers.GameHandler;
import com.example.ckoa.managers.GameStatsManager;
import com.example.ckoa.managers.RestCountriesAPI;
import com.example.ckoa.models.Country;
import com.example.ckoa.models.CountryGuess;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyShapeActivity extends AppCompatActivity {

    private GeoShapeView geoShapeView;
    private AutoCompleteTextView inputCountry;
    private ListView listHistory;
    private Button btnGuess;
    private ImageButton btnBack;

    private GameHandler gameHandler;
    private DatabaseHelper dbHelper;
    private GameStatsManager gameStatsManager;

    private String targetIso3;
    private String targetName;
    private double targetLat;
    private double targetLon;

    private ArrayAdapter<String> historyAdapter;
    private List<String> historyList;
    private List<String> allCountryNames;

    private int attemptsCount = 0;
    private boolean isGameFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_game);

        geoShapeView = findViewById(R.id.geoShapeView);
        inputCountry = findViewById(R.id.inputCountry);
        listHistory = findViewById(R.id.listHistory);
        btnGuess = findViewById(R.id.btnGuess);
        btnBack = findViewById(R.id.btnBack);

        gameHandler = new GameHandler(this);
        dbHelper = new DatabaseHelper(this);
        gameStatsManager = new GameStatsManager(this);

        historyList = new ArrayList<>();
        historyAdapter = new ArrayAdapter<>(this, R.layout.item_guess, historyList);
        listHistory.setAdapter(historyAdapter);

        setupGameData();
        setupAutoComplete();
        loadHistory();

        btnBack.setOnClickListener(v -> finish());
        btnGuess.setOnClickListener(v -> processGuess());
    }

    private void setupGameData() {
        int dailyId = getDailyCountryId();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_COUNTRY_BASE + " LIMIT 1 OFFSET " + (dailyId - 1);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            targetIso3 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ISO3));
            targetName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_NAME_FR));
            String geoJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_GEOSHAPE));
            targetLat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CENTROID_LAT));
            targetLon = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CENTROID_LON));
            geoShapeView.setGeoJson(geoJson);
            cursor.close();
        }
        db.close();
    }

    private void setupAutoComplete() {
        allCountryNames = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.KEY_NAME_FR + " FROM " + DatabaseHelper.TABLE_COUNTRY_BASE, null);
        if (cursor.moveToFirst()) {
            do {
                allCountryNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allCountryNames);
        inputCountry.setAdapter(adapter);
    }

    private void loadHistory() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        Cursor cursor = gameHandler.getGuessesForDate(today);
        if (cursor.moveToFirst()) {
            do {
                attemptsCount++;
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_NAME_FR));
                double dist = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DISTANCE_KM));
                float bearing = cursor.getFloat(cursor.getColumnIndexOrThrow("bearing_deg"));
                int isCorrect = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_IS_CORRECT));

                String emoji = isCorrect == 1 ? "🏆" : "❌";
                String arrow = isCorrect == 1 ? "" : getDirectionArrow(bearing);

                String historyItem = isCorrect == 1
                        ? String.format("%s %s - Gagné !", emoji, name)
                        : String.format("%s %s : %.0f km %s", emoji, name, dist, arrow);

                historyList.add(0, historyItem);

                if (isCorrect == 1 || attemptsCount >= 6) {
                    endGame();
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        historyAdapter.notifyDataSetChanged();
    }

    private void processGuess() {
        if (isGameFinished) return;

        String guessName = inputCountry.getText().toString().trim();
        if (guessName.isEmpty()) return;

        if (!allCountryNames.contains(guessName)) {
            Toast.makeText(this, "Pays inconnu", Toast.LENGTH_SHORT).show();
            return;
        }

        attemptsCount++;
        boolean isWin = guessName.equalsIgnoreCase(targetName);

        saveGuessToDb(guessName, isWin);

        if (isWin) {
            historyList.add(0, "🏆 " + targetName + " - Gagné !");

            gameStatsManager.saveGameResult(attemptsCount, true);
            endGame();

        } else {
            if (attemptsCount >= 6) {
                gameStatsManager.saveGameResult(attemptsCount, false);
                endGame();
                Toast.makeText(this, "Perdu ! C'était : " + targetName, Toast.LENGTH_LONG).show();
            } else {
                inputCountry.setText("");
            }
        }
        historyAdapter.notifyDataSetChanged();
    }

    private void endGame() {
        isGameFinished = true;
        btnGuess.setEnabled(false);
        inputCountry.setEnabled(false);
        transitionToFlagGame();
    }

    private void transitionToFlagGame() {
        // Toast.makeText(this, "Chargement du niveau suivant...", Toast.LENGTH_SHORT).show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            RestCountriesAPI api = new RestCountriesAPI();
            Country countryData = api.getCountry(targetIso3);

            mainHandler.post(() -> {
                if (countryData != null) {
                    Intent intent = new Intent(DailyShapeActivity.this, DailyFlagActivity.class);
                    intent.putExtra("TARGET_ISO3", countryData.getIso3());
                    intent.putExtra("TARGET_NAME", countryData.getFrenchName());
                    intent.putExtra("TARGET_FLAG_URL", countryData.getFlag());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(DailyShapeActivity.this, "Erreur de connexion pour le niveau suivant", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void saveGuessToDb(String guessName, boolean isWin) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.KEY_ISO3 + ", " + DatabaseHelper.KEY_CENTROID_LAT + ", " + DatabaseHelper.KEY_CENTROID_LON +
                " FROM " + DatabaseHelper.TABLE_COUNTRY_BASE +
                " WHERE " + DatabaseHelper.KEY_NAME_FR + " = ?", new String[]{guessName});

        if (cursor.moveToFirst()) {
            String guessIso = cursor.getString(0);
            double guessLat = cursor.getDouble(1);
            double guessLon = cursor.getDouble(2);
            cursor.close();

            float[] results = new float[2];
            Location.distanceBetween(guessLat, guessLon, targetLat, targetLon, results);
            double distanceKm = results[0] / 1000.0;
            double bearing = results[1];

            CountryGuess guess = new CountryGuess(guessIso, distanceKm, bearing, isWin);
            gameHandler.addGuess(guess);

            if (!isWin) {
                String arrow = getDirectionArrow((float)bearing);
                String historyItem = String.format("❌ %s : %.0f km %s", guessName, distanceKm, arrow);
                historyList.add(0, historyItem);
            }
        }
        db.close();
    }

    private String getDirectionArrow(float bearing) {
        String arrows = "⬆↗➡↘⬇↙⬅↖";
        int index = Math.round(((bearing % 360) + 360) % 360 / 45);
        return String.valueOf(arrows.charAt(index % 8));
    }

    public static int getDailyCountryId() {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        int seed = year * 1000 + dayOfYear;
        Random random = new Random(seed);
        return random.nextInt(193) + 1;
    }
}