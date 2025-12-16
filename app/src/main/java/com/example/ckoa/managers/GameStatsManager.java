package com.example.ckoa.managers;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GameStatsManager {

    private static final String KEY_STREAK = "current_streak";
    private static final String KEY_DATE = "date_last_win";
    private static final String KEY_GUESS_1 = "wins_in_1";
    private static final String KEY_GUESS_2 = "wins_in_2";
    private static final String KEY_GUESS_3 = "wins_in_3";
    private static final String KEY_GUESS_4 = "wins_in_4";
    private static final String KEY_GUESS_5 = "wins_in_5";
    private static final String KEY_GUESS_6 = "wins_in_6";
    private static final String KEY_FAIL = "failures";

    private final SharedPreferences prefs;

    public GameStatsManager(Context context) {
        this.prefs = context.getSharedPreferences("ckoa_game_stats", Context.MODE_PRIVATE);
    }

    public int getStreak() {
        return prefs.getInt(KEY_STREAK, 0);
    }

    public String getDate() {
        return prefs.getString(KEY_DATE, "");
    }

    public int getWinsIn1() {
        return prefs.getInt(KEY_GUESS_1, 0);
    }

    public int getWinsIn2() {
        return prefs.getInt(KEY_GUESS_2, 0);
    }

    public int getWinsIn3() {
        return prefs.getInt(KEY_GUESS_3, 0);
    }

    public int getWinsIn4() {
        return prefs.getInt(KEY_GUESS_4, 0);
    }

    public int getWinsIn5() {
        return prefs.getInt(KEY_GUESS_5, 0);
    }

    public int getWinsIn6() {
        return prefs.getInt(KEY_GUESS_6, 0);
    }

    public int getFailures() {
        return prefs.getInt(KEY_FAIL, 0);
    }

    public void saveGameResult(int attempts, boolean won) {
        SharedPreferences.Editor editor = prefs.edit();

        if (won) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = sdf.format(new Date());
            String lastDate = getDate();
            String yesterday = getYesterdayDate(sdf);

            if (lastDate.equals(yesterday)) {
                editor.putInt(KEY_STREAK, getStreak() + 1);
            }
            editor.putString(KEY_DATE, today);

            switch (attempts) {
                case 1:
                    editor.putInt(KEY_GUESS_1, getWinsIn1() + 1);
                    break;
                case 2:
                    editor.putInt(KEY_GUESS_2, getWinsIn2() + 1);
                    break;
                case 3:
                    editor.putInt(KEY_GUESS_3, getWinsIn3() + 1);
                    break;
                case 4:
                    editor.putInt(KEY_GUESS_4, getWinsIn4() + 1);
                    break;
                case 5:
                    editor.putInt(KEY_GUESS_5, getWinsIn5() + 1);
                    break;
                case 6:
                    editor.putInt(KEY_GUESS_6, getWinsIn6() + 1);
                    break;
            }
        } else {
            editor.putInt(KEY_STREAK, 0);
            editor.putInt(KEY_FAIL, getFailures() + 1);
        }

        editor.apply();
    }

    private String getYesterdayDate(SimpleDateFormat sdf) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return sdf.format(cal.getTime());
    }
}