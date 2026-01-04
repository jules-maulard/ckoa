package com.example.ckoa.managers.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.ckoa.models.CountryBase;
import com.example.ckoa.models.CountryMeta;
import com.example.ckoa.models.Guess;

import java.util.ArrayList;
import java.util.List;

public class GameRepository {

    private static final String TAG = "GameRepository";

    private final DatabaseHelper dbHelper;
    private final RestCountriesAPI apiService;

    public GameRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
        this.apiService = new RestCountriesAPI();
    }

    public void prepareDatabase() {
        dbHelper.getWritableDatabase();
        dbHelper.close();
    }

    public List<String> getRandomIsoCodes(int limit) {
        List<String> isoCodes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " + DatabaseInitializer.KEY_ISO3 +
                " FROM " + DatabaseInitializer.TABLE_COUNTRY_BASE +
                " ORDER BY RANDOM() LIMIT " + limit;

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                do {
                    isoCodes.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        }
        return isoCodes;
    }

    public String getCountryNameFr(String iso) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String name = "";

        Cursor cursor = db.query(
                DatabaseInitializer.TABLE_COUNTRY_BASE,
                new String[]{DatabaseInitializer.KEY_NAME_FR},
                DatabaseInitializer.KEY_ISO3 + "=?",
                new String[]{iso},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    public long addGuess(Guess guess) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseInitializer.KEY_GAME_DATE, guess.getGame_date());
        values.put(DatabaseInitializer.KEY_ATTEMPT, getAttemptNumber(db, guess.getGame_date()) + 1);
        values.put(DatabaseInitializer.KEY_GUESSED_ISO3, guess.getIso3());
        values.put(DatabaseInitializer.KEY_DISTANCE_KM, guess.getDistance_km());
        values.put(DatabaseInitializer.KEY_BEARING_DEG, guess.getBearing_deg());
        values.put(DatabaseInitializer.KEY_IS_CORRECT, guess.getIs_correct());

        long id = db.insert(DatabaseInitializer.TABLE_GUESS, null, values);
        db.close();
        return id;
    }

    public Cursor getGuessesForDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT g.*, c." + DatabaseInitializer.KEY_NAME_FR +
                " FROM " + DatabaseInitializer.TABLE_GUESS + " g " +
                " JOIN " + DatabaseInitializer.TABLE_COUNTRY_BASE + " c " +
                " ON g." + DatabaseInitializer.KEY_GUESSED_ISO3 + " = c." + DatabaseInitializer.KEY_ISO3 +
                " WHERE g." + DatabaseInitializer.KEY_GAME_DATE + " = ?" +
                " ORDER BY g." + DatabaseInitializer.KEY_ATTEMPT + " ASC";

        return db.rawQuery(query, new String[]{date});
    }

    private int getAttemptNumber(SQLiteDatabase db, String gameDate) {
        int count = 0;
        String countQuery = "SELECT COUNT(*) FROM " + DatabaseInitializer.TABLE_GUESS +
                " WHERE " + DatabaseInitializer.KEY_GAME_DATE + " = ?";

        try (Cursor cursor = db.rawQuery(countQuery, new String[]{gameDate})) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        return count;
    }


    public CountryMeta getCountryMeta(String iso3) {
        CountryMeta localMeta = getMetaFromDb(iso3);
        if (localMeta != null) {
            return localMeta;
        }

        CountryMeta remoteMeta = apiService.fetchCountryMeta(iso3);
        if (remoteMeta != null) {
            saveCountryMeta(remoteMeta);
        }

        return remoteMeta;
    }

    private void saveCountryMeta(CountryMeta meta) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseInitializer.KEY_ISO3, meta.getIso3());
        values.put(DatabaseInitializer.KEY_CAPITAL, meta.getCapital());
        values.put(DatabaseInitializer.KEY_CURRENCY, meta.getCurrency());
        values.put(DatabaseInitializer.KEY_POPULATION, meta.getPopulation());
        values.put(DatabaseInitializer.KEY_FLAG_URL, meta.getFlag());

        // Conversion Array -> String (ex: ["fr","en"] -> "fr,en")
        values.put(DatabaseInitializer.KEY_LANGUAGES, arrayToString(meta.getLanguages()));
        values.put(DatabaseInitializer.KEY_NEIGHBORS, arrayToString(meta.getBorders()));

        db.insertWithOnConflict(DatabaseInitializer.TABLE_COUNTRY_META, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    private CountryMeta getMetaFromDb(String iso3) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        CountryMeta meta = null;

        String query = "SELECT * FROM " + DatabaseInitializer.TABLE_COUNTRY_META +
                " WHERE " + DatabaseInitializer.KEY_ISO3 + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{iso3})) {
            if (cursor.moveToFirst()) {
                String capital = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_CAPITAL));
                String currency = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_CURRENCY));
                long population = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_POPULATION));
                String flag = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_FLAG_URL));

                String rawLanguages = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_LANGUAGES));
                String rawNeighbors = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_NEIGHBORS));

                meta = new CountryMeta(
                        iso3,
                        capital,
                        currency,
                        stringToArray(rawLanguages),
                        population,
                        flag,
                        stringToArray(rawNeighbors)
                );
            }
        }
        return meta;
    }

    public CountryBase getCountryByOffset(int offset) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseInitializer.TABLE_COUNTRY_BASE +
                " LIMIT 1 OFFSET " + offset;

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                return mapCursorToCountryBase(cursor);
            }
        }
        return null;
    }

    public CountryBase getCountryByName(String nameFr) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseInitializer.TABLE_COUNTRY_BASE +
                " WHERE " + DatabaseInitializer.KEY_NAME_FR + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{nameFr})) {
            if (cursor.moveToFirst()) {
                return mapCursorToCountryBase(cursor);
            }
        }
        return null;
    }

    public List<String> getAllCountryNames() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DatabaseInitializer.KEY_NAME_FR + " FROM " + DatabaseInitializer.TABLE_COUNTRY_BASE, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private CountryBase mapCursorToCountryBase(Cursor cursor) {
        return new CountryBase(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_ISO3)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_NAME_EN)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_NAME_FR)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_CENTROID_LAT)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_CENTROID_LON)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseInitializer.KEY_GEOSHAPE))
        );
    }

    private String arrayToString(String[] array) {
        if (array == null || array.length == 0) return "";
        return TextUtils.join(",", array);
    }

    private String[] stringToArray(String raw) {
        if (raw == null || raw.isEmpty()) return new String[0];
        return raw.split(",");
    }
}