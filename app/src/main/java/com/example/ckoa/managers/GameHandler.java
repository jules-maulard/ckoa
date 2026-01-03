package com.example.ckoa.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.ckoa.models.Country;
import com.example.ckoa.models.CountryGuess;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.example.ckoa.R;

public class GameHandler {
    DatabaseHelper dbHelper;

    public GameHandler(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }


    public void initData(Context context) {
        Log.d("DEBUG_DB", "Démarrage de initData");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Vérification base vide
        Cursor cursor = db.rawQuery("SELECT count(*) FROM " + DatabaseHelper.TABLE_COUNTRY_BASE, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        if (count > 0) {
            Log.d("DEBUG_DB", "La base est déjà remplie (" + cursor.getInt(0) + " pays). On arrête.");
            cursor.close();
            db.close();
            return;
        }
        Log.d("DEBUG_DB", "La base est vide, on commence l'import.");

        try {
            InputStream is = context.getResources().openRawResource(R.raw.world_administrative_boundaries);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();
            Log.d("DEBUG_DB", "Fichier lu.");

            JSONArray jsonArray = new JSONArray(jsonString.toString());
            Log.d("DEBUG_DB", "JSON chargé ! Nombre d'entrées trouvées : " + jsonArray.length());

            db.beginTransaction();
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    if (!obj.getString("status").equals("Member State")) {
                        Log.d("DEBUG_DB", "Pays non membre : " + obj.getString("name"));
                        continue;
                    }

                    ContentValues values = new ContentValues();

                    values.put(DatabaseHelper.KEY_ISO3, obj.getString("iso3"));
                    values.put(DatabaseHelper.KEY_NAME_EN, obj.getString("name"));
                    values.put(DatabaseHelper.KEY_NAME_FR, obj.getString("french_short"));
                    JSONObject geo = obj.getJSONObject("geo_point_2d");
                    values.put(DatabaseHelper.KEY_CENTROID_LAT, geo.getDouble("lat"));
                    values.put(DatabaseHelper.KEY_CENTROID_LON, geo.getDouble("lon"));
                    values.put(DatabaseHelper.KEY_GEOSHAPE, obj.optString("geo_shape", ""));

                    db.insert(DatabaseHelper.TABLE_COUNTRY_BASE, null, values);
                }
                db.setTransactionSuccessful();
                Log.d("DEBUG_DB", "Transaction réussie ! Tout est inséré.");
            } catch (Exception e) {
                Log.e("DEBUG_DB", "ERREUR DANS LA BOUCLE FOR : " + e.getMessage());
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }

        } catch (Exception e) {
            Log.e("DEBUG_DB", "GROS CRASH : " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public long addGuess(CountryGuess guess) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.KEY_GAME_DATE, guess.getGame_date());
        values.put(DatabaseHelper.KEY_ATTEMPT, getNumberAttempt(db, guess.getGame_date()));
        values.put(DatabaseHelper.KEY_GUESSED_ISO3, guess.getIso3());
        values.put(DatabaseHelper.KEY_DISTANCE_KM, guess.getDistance_km());
        values.put(DatabaseHelper.KEY_BEARING_DEG, guess.getBearing_deg());
        values.put(DatabaseHelper.KEY_IS_CORRECT, guess.getIs_correct());

        long id = db.insert(DatabaseHelper.TABLE_GUESS, null, values);

        db.close();
        return id;
    }

    public Cursor getGuessesForDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT g.*, c." + DatabaseHelper.KEY_NAME_FR +
                " FROM " + DatabaseHelper.TABLE_GUESS + " g " +
                " JOIN " + DatabaseHelper.TABLE_COUNTRY_BASE + " c " +
                " ON g." + DatabaseHelper.KEY_GUESSED_ISO3 + " = c." + DatabaseHelper.KEY_ISO3 +
                " WHERE g." + DatabaseHelper.KEY_GAME_DATE + " = ?" +
                " ORDER BY g." + DatabaseHelper.KEY_ATTEMPT + " ASC";

        return db.rawQuery(query, new String[]{date});
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


    public List<String> getRandomIsoCodes(int limit) {
        List<String> isoCodes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + DatabaseHelper.KEY_ISO3 +
                        " FROM " + DatabaseHelper.TABLE_COUNTRY_BASE +
                        " ORDER BY RANDOM() LIMIT " + limit,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                isoCodes.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return isoCodes;
    }

    public String getCountryName(String iso) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String name = "";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_COUNTRY_BASE,
                new String[]{DatabaseHelper.KEY_NAME_FR},
                DatabaseHelper.KEY_ISO3 + "=?",
                new String[]{iso},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }
}
