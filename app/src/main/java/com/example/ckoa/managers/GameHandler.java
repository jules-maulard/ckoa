package com.example.ckoa.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.ckoa.data.CountryGuess;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GameHandler {
    DatabaseHelper dbHelper;

    public GameHandler(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public long addCountryBase(String iso3, String name_en, String name_fr, Double centroid_lat, Double centroid_lon, String geoshape) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.KEY_ISO3, iso3);
        values.put(DatabaseHelper.KEY_NAME_EN, name_en);
        values.put("name_fr", name_fr);
        values.put("centroid_lat", centroid_lat);
        values.put("centroid_lon", centroid_lon);
        values.put("geoshape", geoshape);

        long id = db.replace(DatabaseHelper.TABLE_COUNTRY_BASE, null, values);

        db.close();
        return id;
    }

    public long addGuess(CountryGuess guess) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String gameDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        values.put(DatabaseHelper.KEY_GAME_DATE, gameDate);
        values.put(DatabaseHelper.KEY_ATTEMPT, getNumberAttempt(db, gameDate));
        values.put(DatabaseHelper.KEY_GUESSED_ISO3, guess.getIso3());
        values.put(DatabaseHelper.KEY_DISTANCE_KM, guess.get);
        values.put("bearing_deg", guess.getBearingDeg());
        values.put(DatabaseHelper.KEY_IS_CORRECT, guess.isCorrect() ? 1 : 0);

        long id = db.insert(DatabaseHelper.TABLE_GUESS, null, values);

        db.close();
        return id;
    }

    private int getNumberAttempt(SQLiteDatabase db, String gameDate) {
        int count = 0;

        String countQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_GUESS +
                " WHERE " + DatabaseHelper.KEY_GAME_DATE + " = ?";

        Cursor cursor = db.rawQuery(countQuery, new String[]{gameDate});

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }
}
}