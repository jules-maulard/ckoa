package com.example.ckoa.models;

public class CountryMeta {

    private final String iso3;
    private final String capital;
    private final String currency;
    private final String[] languages;
    private final Long population;
    private final String flag;
    private final String[] borders;

    public CountryMeta(String iso3, String capital, String currency, String[] languages, Long population, String flag, String[] borders) {
        this.iso3 = iso3;
        this.capital = capital;
        this.currency = currency;
        this.languages = languages;
        this.population = population;
        this.flag = flag;
        this.borders = borders;
    }

    public String getIso3() {
        return iso3;
    }

    public String getCapital() {
        return capital;
    }

    public String getCurrency() {
        return currency;
    }

    public String[] getLanguages() {
        return languages;
    }

    public Long getPopulation() {
        return population;
    }

    public String getFlag() {
        return flag;
    }

    public String[] getBorders() {
        return borders;
    }
}
