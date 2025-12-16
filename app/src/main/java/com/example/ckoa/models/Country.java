package com.example.ckoa.models;

public class Country {
    private final String iso3;
    private final String englishName;
    private final String frenchName;
    private final String flag;
    private final String[] borders;
    private final String currency;
    private final String capital;
    private final String[] languages;
    private final Long population;

    public Country(String iso3, String englishName, String frenchName, String flag, String[] borders, String currency, String capital, String[] languages, Long population) {
        this.iso3 = iso3;
        this.englishName = englishName;
        this.frenchName = frenchName;
        this.flag = flag;
        this.borders = borders;
        this.currency = currency;
        this.capital = capital;
        this.languages = languages;
        this.population = population;
    }

    public String getIso3() {
        return iso3;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getFrenchName() {
        return frenchName;
    }

    public String getFlag() {
        return flag;
    }

    public String[] getBorders() {
        return borders;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCapital() {
        return capital;
    }

    public String[] getLanguages() {
        return languages;
    }

    public Long getPopulation() {
        return population;
    }
}
