package com.example.ckoa.models;

public class CountryBase {
    private final String iso3;
    private final String nameEn;
    private final String nameFr;
    private final double centroidLat;
    private final double centroidLon;
    private final String geoShape;

    public CountryBase(String iso3, String nameEn, String nameFr, double centroidLat, double centroidLon, String geoShape) {
        this.iso3 = iso3;
        this.nameEn = nameEn;
        this.nameFr = nameFr;
        this.centroidLat = centroidLat;
        this.centroidLon = centroidLon;
        this.geoShape = geoShape;
    }

    public String getIso3() {
        return iso3;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameFr() {
        return nameFr;
    }

    public double getCentroidLat() {
        return centroidLat;
    }

    public double getCentroidLon() {
        return centroidLon;
    }

    public String getGeoShape() {
        return geoShape;
    }
}