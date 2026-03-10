package com.example.ckoa.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ckoa.models.DailyStep;
import com.example.ckoa.models.FlagGuess;
import com.example.ckoa.models.ShapeGuess;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProgressRepository {

    private static final String PREF_NAME = "DailyProgressPrefs";
    private static final String KEY_LAST_ACCESS_DATE = "lastAccessDate";
    private static final String KEY_CURRENT_STEP = "currentStep";
    private static final String KEY_SHAPE_GUESSES = "shapeGuesses";
    private static final String KEY_FLAG_GUESSES = "flagGuesses";

    private final SharedPreferences preferences;
    private final Gson gson;

    public ProgressRepository(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        if (isNewDay()) {
            clearProgressForNewDay();
        }
    }

    private boolean isNewDay() {
        long lastAccessDate = preferences.getLong(KEY_LAST_ACCESS_DATE, 0);
        return !isToday(lastAccessDate);
    }

    private void clearProgressForNewDay() {
        preferences.edit()
                .putString(KEY_CURRENT_STEP, DailyStep.SHAPE.name())
                .remove(KEY_SHAPE_GUESSES)
                .remove(KEY_FLAG_GUESSES)
                .putLong(KEY_LAST_ACCESS_DATE, System.currentTimeMillis())
                .apply();
    }

    private boolean isToday(long timestamp) {
        if (timestamp == 0) return false;

        Calendar today = Calendar.getInstance();
        Calendar lastAccess = Calendar.getInstance();
        lastAccess.setTimeInMillis(timestamp);

        return today.get(Calendar.YEAR) == lastAccess.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == lastAccess.get(Calendar.DAY_OF_YEAR);
    }

    public void saveCurrentStep(DailyStep step) {
        preferences.edit().putString(KEY_CURRENT_STEP, step.name()).apply();
    }

    public DailyStep getCurrentStep() {
        String stepString = preferences.getString(KEY_CURRENT_STEP, DailyStep.SHAPE.name());
        return DailyStep.valueOf(stepString);
    }

    public void saveShapeGuesses(List<ShapeGuess> shapeGuesses) {
        String json = gson.toJson(shapeGuesses);
        preferences.edit().putString(KEY_SHAPE_GUESSES, json).apply();
    }

    private List<ShapeGuess> getShapeGuesses() {
        String json = preferences.getString(KEY_SHAPE_GUESSES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<ShapeGuess>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveFlagGuesses(List<FlagGuess> flagGuesses) {
        String json = gson.toJson(flagGuesses);
        preferences.edit().putString(KEY_FLAG_GUESSES, json).apply();
    }

    private List<FlagGuess> getFlagGuesses() {
        String json = preferences.getString(KEY_FLAG_GUESSES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<FlagGuess>>() {}.getType();
        return gson.fromJson(json, type);
    }
}