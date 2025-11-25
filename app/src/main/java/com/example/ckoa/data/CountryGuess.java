package com.example.ckoa.data;

public class CountryGuess {
    private final String iso3;
    private final String englishName;
    private final String frenchName;

    public CountryGuess(String iso3, String englishName, String frenchName) {
        this.iso3 = iso3;
        this.englishName = englishName;
        this.frenchName = frenchName;
    }

    public String getIso3() {
        return iso3;
    }

    public String getFrenchName() {
        return frenchName;
    }

    public String getEnglishName() {
        return englishName;
    }
}
