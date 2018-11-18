package com.example.amira.openweatherapp.models;

import com.google.android.gms.maps.model.LatLng;

public class SelectedUserLocation {

    private String Name;
    private LatLng Position;

    public void setName(String name) {
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public void setPosition(LatLng position) {
        Position = position;
    }

    public LatLng getPosition() {
        return Position;
    }
}
