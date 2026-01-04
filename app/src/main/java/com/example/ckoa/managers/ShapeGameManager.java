package com.example.ckoa.managers;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;

import com.example.ckoa.managers.data.DatabaseInitializer;
import com.example.ckoa.managers.data.GameRepository;
import com.example.ckoa.models.CountryBase;
import com.example.ckoa.models.CountryMeta;
import com.example.ckoa.models.Guess;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ShapeGameManager {

    private final GameRepository repository;

    private CountryBase targetCountry;
    private final String todayDate;

    public ShapeGameManager(Context context) {
        this.repository = new GameRepository(context);
        this.todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public String loadDailyTarget() {
        int dailyId = getDailyCountryId();
        // offset SQL commence à 0, donc dailyId - 1
        this.targetCountry = repository.getCountryByOffset(dailyId - 1);

        if (this.targetCountry != null) {
            return this.targetCountry.getGeoShape();
        }
        return null;
    }

    public List<String> getAllCountryNames() {
        return repository.getAllCountryNames();
    }

    public Cursor getTodayHistory() {
        return repository.getGuessesForDate(todayDate);
    }

    public List<Guess> getHistoryGuesses() {
        List<Guess> guesses = new ArrayList<>();
        Cursor cursor = repository.getGuessesForDate(todayDate);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Conversion propre : SQL -> Objet Java
                Guess guess = new Guess(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_GUESSED_ISO3)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_DISTANCE_KM)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_BEARING_DEG)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_IS_CORRECT)) == 1
                );
                guesses.add(guess);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return guesses;
    }

    public Guess makeGuess(String guessName) {
        if (targetCountry == null) return null;

        // Récupérer les infos du pays deviné
        CountryBase guessCountry = repository.getCountryByName(guessName);
        if (guessCountry == null) {
            return null; // Pays inconnu
        }

        // Calculer distance et bearing
        float[] results = new float[2];
        Location.distanceBetween(
                guessCountry.getCentroidLat(), guessCountry.getCentroidLon(),
                targetCountry.getCentroidLat(), targetCountry.getCentroidLon(),
                results
        );
        double distanceKm = results[0] / 1000.0;
        double bearing = results[1];

        // Vérifier victoire
        boolean isWin = guessCountry.getIso3().equals(targetCountry.getIso3());

        // Créer et sauvegarder le Guess
        Guess guess = new Guess(
                guessCountry.getIso3(),
                distanceKm,
                bearing,
                isWin
        );

        repository.addGuess(guess);

        return guess;
    }

    public String getTargetName() {
        return targetCountry != null ? targetCountry.getNameFr() : "";
    }


    public String getDirectionArrow(float bearing) {
        String arrows = "⬆↗➡↘⬇↙⬅↖";
        int index = Math.round(((bearing % 360) + 360) % 360 / 45);
        return String.valueOf(arrows.charAt(index % 8));
    }

    private int getDailyCountryId() {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        int seed = year * 1000 + dayOfYear;
        Random random = new Random(seed);
        return random.nextInt(193) + 1;
    }

    public String getCountryNameByIso(String iso) {
        return repository.getCountryNameFr(iso);
    }
}