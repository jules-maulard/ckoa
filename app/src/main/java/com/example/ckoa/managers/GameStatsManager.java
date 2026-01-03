package com.example.ckoa.managers;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GameStatsManager {

    private static final String KEY_CURRENT_STREAK_COUNT = "current_streak";
    private static final String KEY_LAST_GAME_PLAYED_DATE = "date_last_game";
    private static final String KEY_PREFIX_WINS_BY_ATTEMPTS = "wins_in_";
    private static final String KEY_GAME_FAILURE_COUNT = "failures";

    private final SharedPreferences sharedPreferences;

    public GameStatsManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences("ckoa_game_stats", Context.MODE_PRIVATE);
    }

    public int getCurrentStreak() { return sharedPreferences.getInt(KEY_CURRENT_STREAK_COUNT, 0); }
    public int getTotalFailures() { return sharedPreferences.getInt(KEY_GAME_FAILURE_COUNT, 0); }
    public String getLastPlayDate() { return sharedPreferences.getString(KEY_LAST_GAME_PLAYED_DATE, ""); }
    public int getWinsForAttemptsCount(int attempts) {
        return sharedPreferences.getInt(KEY_PREFIX_WINS_BY_ATTEMPTS + attempts, 0);
    }


    public void saveGameResult(int attempts, boolean isVictory) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String today = sdf.format(new Date());
        String lastDate = getLastPlayDate();
        if (today.equals(lastDate)) {
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LAST_GAME_PLAYED_DATE, today);

        if (!isVictory) {
            editor.putInt(KEY_CURRENT_STREAK_COUNT, 0);
            editor.putInt(KEY_GAME_FAILURE_COUNT, getTotalFailures() + 1);
            editor.apply();
            return;
        }

        String yesterday = getYesterdayDate(sdf);
        if (lastDate.equals(yesterday)) {
            editor.putInt(KEY_CURRENT_STREAK_COUNT, getCurrentStreak() + 1);
        } else {
            editor.putInt(KEY_CURRENT_STREAK_COUNT, 1);
        }

        editor.putInt(KEY_PREFIX_WINS_BY_ATTEMPTS + attempts, getWinsForAttemptsCount(attempts) + 1);
        editor.apply();
    }

    private String getYesterdayDate(SimpleDateFormat sdf) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return sdf.format(cal.getTime());
    }
}