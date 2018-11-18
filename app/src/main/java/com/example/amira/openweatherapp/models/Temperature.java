package com.example.amira.openweatherapp.models;

public class Temperature {
    private double Day;
    private double Min;
    private double Max;

    public void setDay(double day) {
        Day = day;
    }

    public double getDay() {
        return Day;
    }

    public void setMax(double max) {
        Max = max;
    }

    public double getMax() {
        return Max;
    }

    public void setMin(double min) {
        Min = min;
    }

    public double getMin() {
        return Min;
    }
}
