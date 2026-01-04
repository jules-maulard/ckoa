package com.example.ckoa.managers.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.ckoa.R;
import com.example.ckoa.models.CountryBase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSeeder {

    private static final String TAG = "DB_SEEDER";

    public static void seedDatabase(SQLiteDatabase db, Context context) {

        if (!isDatabaseEmpty(db)) {
            Log.d(TAG, "Database already populated. Skipping seed.");
            return;
        }
        Log.i(TAG, "Database is empty. Starting seeding from JSON...");

        db.beginTransaction();
        try {
            List<CountryBase> countries = loadDataFromJson(context);

            for (CountryBase country : countries) {
                ContentValues values = createContentValues(country);
                db.insert(DatabaseInitializer.TABLE_COUNTRY_BASE, null, values);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, "Seeding completed successfully. " + countries.size() + " countries inserted.");

        } catch (Exception e) {
            Log.e(TAG, "Error during database seeding", e);
        } finally {
            db.endTransaction();
        }
    }

    private static boolean isDatabaseEmpty(SQLiteDatabase db) {
        String query = "SELECT count(*) FROM " + DatabaseInitializer.TABLE_COUNTRY_BASE;
        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) == 0;
            }
            return true;
        }
    }

    private static ContentValues createContentValues(CountryBase country) {
        ContentValues values = new ContentValues();
        values.put(DatabaseInitializer.KEY_ISO3, country.getIso3());
        values.put(DatabaseInitializer.KEY_NAME_EN, country.getNameEn());
        values.put(DatabaseInitializer.KEY_NAME_FR, country.getNameFr());
        values.put(DatabaseInitializer.KEY_CENTROID_LAT, country.getCentroidLat());
        values.put(DatabaseInitializer.KEY_CENTROID_LON, country.getCentroidLon());
        values.put(DatabaseInitializer.KEY_GEOSHAPE, country.getGeoShape());
        return values;
    }

    private static List<CountryBase> loadDataFromJson(Context context) throws Exception {
        List<CountryBase> list = new ArrayList<>();

        String jsonString = readJsonStringFromResource(context);
        JSONArray jsonArray = new JSONArray(jsonString);

        Log.d(TAG, "JSON loaded. Entries found: " + jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);

            if (!isMemberState(obj)) {
                continue;
            }

            JSONObject geoPoint = obj.getJSONObject("geo_point_2d");

            CountryBase data = new CountryBase(
                    obj.getString("iso3"),
                    obj.getString("name"),
                    obj.optString("french_short", obj.getString("name")),
                    geoPoint.getDouble("lat"),
                    geoPoint.getDouble("lon"),
                    obj.optString("geo_shape", "")
            );

            list.add(data);
        }
        return list;
    }

    private static String readJsonStringFromResource(Context context) throws java.io.IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.world_administrative_boundaries);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        reader.close();
        return jsonBuilder.toString();
    }

    private static boolean isMemberState(JSONObject obj) {
        return "Member State".equals(obj.optString("status"));
    }
}