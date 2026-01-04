package com.example.ckoa.managers.data;

import android.util.Log;

import com.example.ckoa.models.CountryMeta;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RestCountriesAPI {
    private static final String TAG = "RestCountriesAPI";
    private static final String BASE_URL = "https://restcountries.com/v3.1/alpha/";
    private static final int TIMEOUT_MS = 5000;

    public CountryMeta fetchCountryMeta(String iso3) {
        try {
            JSONArray responseArray = fetchJsonFromUrl(BASE_URL + iso3);
            if (responseArray == null || responseArray.length() == 0) {
                return null;
            }

            JSONObject jsonObject = responseArray.getJSONObject(0);

            String capital = parseCapital(jsonObject);
            String currency = parseCurrency(jsonObject);
            String[] languages = parseLanguages(jsonObject);
            Long population = jsonObject.optLong("population", 0);
            String flagUrl = parseFlag(jsonObject);
            String[] borders = parseBorders(jsonObject);

            return new CountryMeta(
                    iso3,
                    capital,
                    currency,
                    languages,
                    population,
                    flagUrl,
                    borders
            );

        } catch (Exception e) {
            Log.e(TAG, "Error fetching country meta for ISO: " + iso3, e);
            return null;
        }
    }

    private JSONArray fetchJsonFromUrl(String urlString) throws IOException, JSONException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Log.e(TAG, "HTTP Error: " + responseCode);
                return null;
            }

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return new JSONArray(response.toString());

        } finally {
            if (connection != null) connection.disconnect();
            if (reader != null) reader.close();
        }
    }

    private String parseCapital(JSONObject data) {
        JSONArray capitalArray = data.optJSONArray("capital");
        if (capitalArray != null && capitalArray.length() > 0) {
            return capitalArray.optString(0);
        }
        return null;
    }

    private String parseCurrency(JSONObject data) {
        JSONObject currencies = data.optJSONObject("currencies");
        if (currencies == null) return null;

        Iterator<String> keys = currencies.keys();
        if (keys.hasNext()) {
            String currencyCode = keys.next();
            JSONObject currencyObj = currencies.optJSONObject(currencyCode);
            if (currencyObj != null) {
                String name = currencyObj.optString("name", "");
                return name + " (" + currencyCode + ")";
            }
        }
        return null;
    }

    private String[] parseLanguages(JSONObject data) {
        JSONObject languagesObj = data.optJSONObject("languages");
        if (languagesObj == null) return new String[0];

        List<String> list = new ArrayList<>();
        Iterator<String> keys = languagesObj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            list.add(languagesObj.optString(key));
        }
        return list.toArray(new String[0]);
    }

    private String parseFlag(JSONObject data) {
        JSONObject flags = data.optJSONObject("flags");
        if (flags != null) {
            return flags.optString("png", null);
        }
        return null;
    }

    private String[] parseBorders(JSONObject data) {
        JSONArray bordersArray = data.optJSONArray("borders");
        if (bordersArray == null) return new String[0];

        String[] borders = new String[bordersArray.length()];
        for (int i = 0; i < bordersArray.length(); i++) {
            borders[i] = bordersArray.optString(i);
        }
        return borders;
    }
}