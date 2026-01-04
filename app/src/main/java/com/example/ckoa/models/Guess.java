package com.example.ckoa.models;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Guess {
    private final String iso3;
    private final String game_date;
    private final Double distance_km;
    private final Double bearing_deg;
    private final Boolean is_correct;

    public Guess(String iso3, Double distance_km, Double bearing_deg, Boolean is_correct) {
        this.iso3 = iso3;
        this.game_date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
        this.distance_km = distance_km;
        this.bearing_deg = bearing_deg;
        this.is_correct = is_correct;
    }

    public String getIso3() {
        return iso3;
    }

    public Boolean getIs_correct() {
        return is_correct;
    }

    public String getGame_date() {
        return game_date;
    }

    public Double getDistance_km() {
        return distance_km;
    }

    public Double getBearing_deg() {
        return bearing_deg;
    }


}
