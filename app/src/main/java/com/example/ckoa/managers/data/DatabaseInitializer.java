package com.example.ckoa.managers.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseInitializer {

    private static final String TAG = "DB_INITIALIZER";

    // Table Constants
    public static final String TABLE_COUNTRY_BASE = "CountryBase";
    public static final String TABLE_COUNTRY_META = "CountryMeta";
    public static final String TABLE_DAILY_GAME = "DailyGame";
    public static final String TABLE_GUESS = "Guess";

    // Column Constants
    public static final String KEY_ISO3 = "iso3";
    public static final String KEY_NAME_EN = "name_en";
    public static final String KEY_NAME_FR = "name_fr";
    public static final String KEY_CENTROID_LAT = "centroid_lat";
    public static final String KEY_CENTROID_LON = "centroid_lon";
    public static final String KEY_GEOSHAPE = "geoshape";

    public static final String KEY_CAPITAL = "capital";
    public static final String KEY_CAPITAL_LAT = "capital_lat";
    public static final String KEY_CAPITAL_LON = "capital_lon";
    public static final String KEY_CURRENCY = "currency";
    public static final String KEY_LANGUAGES = "languages";
    public static final String KEY_POPULATION = "population";
    public static final String KEY_FLAG_URL = "flag_url";
    public static final String KEY_COAT_OF_ARMS_URL = "coat_of_arms_url";
    public static final String KEY_NEIGHBORS = "neighbors";

    public static final String KEY_GAME_DATE = "game_date";
    public static final String KEY_TARGET_ISO3 = "target_iso3";
    public static final String KEY_COMPLETED = "completed";

    public static final String KEY_ID = "id";
    public static final String KEY_ATTEMPT = "attempt";
    public static final String KEY_GUESSED_ISO3 = "guessed_iso3";
    public static final String KEY_DISTANCE_KM = "distance_km";
    public static final String KEY_BEARING_DEG = "bearing_deg";
    public static final String KEY_IS_CORRECT = "is_correct";

    public static void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");

        db.beginTransaction();
        try {
            String createCountryBase = "CREATE TABLE " + TABLE_COUNTRY_BASE + " (" +
                    KEY_ISO3 + " TEXT PRIMARY KEY," +
                    KEY_NAME_EN + " TEXT," +
                    KEY_NAME_FR + " TEXT," +
                    KEY_CENTROID_LAT + " REAL," +
                    KEY_CENTROID_LON + " REAL," +
                    KEY_GEOSHAPE + " TEXT" +
                    ");";
            db.execSQL(createCountryBase);
            Log.d(TAG, "CountryBase table created");

            String createCountryMeta = "CREATE TABLE " + TABLE_COUNTRY_META + " (" +
                    KEY_ISO3 + " TEXT PRIMARY KEY," +
                    KEY_CAPITAL + " TEXT," +
                    KEY_CAPITAL_LAT + " REAL," +
                    KEY_CAPITAL_LON + " REAL," +
                    KEY_CURRENCY + " TEXT," +
                    KEY_LANGUAGES + " TEXT," +
                    KEY_POPULATION + " INTEGER," +
                    KEY_FLAG_URL + " TEXT," +
                    KEY_COAT_OF_ARMS_URL + " TEXT," +
                    KEY_NEIGHBORS + " TEXT," +
                    "FOREIGN KEY(" + KEY_ISO3 + ") REFERENCES " + TABLE_COUNTRY_BASE + "(" + KEY_ISO3 + ")" +
                    ");";
            db.execSQL(createCountryMeta);
            Log.d(TAG, "CountryMeta table created");

            String createDailyGame = "CREATE TABLE " + TABLE_DAILY_GAME + " (" +
                    KEY_GAME_DATE + " DATE PRIMARY KEY," +
                    KEY_TARGET_ISO3 + " TEXT," +
                    KEY_COMPLETED + " INTEGER," +
                    "FOREIGN KEY(" + KEY_TARGET_ISO3 + ") REFERENCES " + TABLE_COUNTRY_BASE + "(" + KEY_ISO3 + ")" +
                    ");";
            db.execSQL(createDailyGame);
            Log.d(TAG, "DailyGame table created");

            String createGuess = "CREATE TABLE " + TABLE_GUESS + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_GAME_DATE + " DATE," +
                    KEY_ATTEMPT + " INTEGER," +
                    KEY_GUESSED_ISO3 + " TEXT," +
                    KEY_DISTANCE_KM + " REAL," +
                    KEY_BEARING_DEG + " REAL," +
                    KEY_IS_CORRECT + " INTEGER," +
                    "FOREIGN KEY(" + KEY_GAME_DATE + ") REFERENCES " + TABLE_DAILY_GAME + "(" + KEY_GAME_DATE + ")," +
                    "FOREIGN KEY(" + KEY_GUESSED_ISO3 + ") REFERENCES " + TABLE_COUNTRY_BASE + "(" + KEY_ISO3 + ")" +
                    ");";
            db.execSQL(createGuess);
            Log.d(TAG, "Guess table created");

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error creating tables", e);
            throw e;
        } finally {
            db.endTransaction();
        }
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GUESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_GAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUNTRY_META);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUNTRY_BASE);

        onCreate(db);
    }
}