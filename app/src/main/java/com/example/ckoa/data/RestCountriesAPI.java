package com.example.ckoa.data;

import android.util.Log;
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
    private static final String REST_COUNTRY_URL = "https://restcountries.com/v3.1/alpha/";

    public Country getCountry(String iso3Country) {
        try {
            JSONArray countryArray = getJsonArrayFromUrl(REST_COUNTRY_URL + iso3Country);
            if (countryArray == null || countryArray.length() == 0) return null;

            JSONObject countryData = countryArray.getJSONObject(0);

            String englishName = countryData.getJSONObject("name").getString("common");
            String frenchName = countryData.getJSONObject("translations").getJSONObject("fra").getString("common");
            String flag = countryData.getJSONObject("flags").getString("png");

            String[] borders = null;
            JSONArray bordersArray = countryData.getJSONArray("borders");
            borders = new String[bordersArray.length()];
            for (int i = 0; i < bordersArray.length(); i++) {
                borders[i] = bordersArray.getString(i);
            }

            String currency = null;
            JSONObject currencies = countryData.getJSONObject("currencies");
            Iterator<String> keys = currencies.keys();
            String currencyCode = keys.next();
            JSONObject currencyObj = currencies.getJSONObject(currencyCode);
            currency = currencyObj.getString("name") + " (" + currencyCode + ")";

            JSONArray capitalArray = countryData.getJSONArray("capital");
            String capital = capitalArray.getString(0);

            String[] languages = null;
            JSONObject languagesObj = countryData.getJSONObject("languages");
            List<String> languagesList = new ArrayList<>();
            Iterator<String> langKeys = languagesObj.keys();
            while (langKeys.hasNext()) {
                String langKey = langKeys.next();
                languagesList.add(languagesObj.getString(langKey));
            }
            languages = languagesList.toArray(new String[0]);

            Integer population = countryData.getInt("population");

            return new Country(
                    iso3Country,
                    englishName,
                    frenchName,
                    flag,
                    borders,
                    currency,
                    capital,
                    languages,
                    population
            );

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération du pays", e);
            return null;
        }
    }

    private JSONArray getJsonArrayFromUrl(String urlString) throws IOException, JSONException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();

            if (connection.getResponseCode() != 200) {
                Log.e(TAG, "Erreur HTTP: " + connection.getResponseCode());
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
}