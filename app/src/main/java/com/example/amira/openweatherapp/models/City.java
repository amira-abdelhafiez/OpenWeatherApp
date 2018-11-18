package com.example.amira.openweatherapp.models;

public class City {
    private int GeoId;
    private String Name;
    private double Latitude;
    private double Longitude;
    private DayWeather[] DayWeathers;

    public void setGeoId(int geoId) {
        GeoId = geoId;
    }

    public int getGeoId() {
        return GeoId;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public void setDayWeathers(DayWeather[] dayWeathers) {
        DayWeathers = dayWeathers;
    }

    public DayWeather[] getDayWeathers() {
        return DayWeathers;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public double getLongitude() {
        return Longitude;
    }
}
