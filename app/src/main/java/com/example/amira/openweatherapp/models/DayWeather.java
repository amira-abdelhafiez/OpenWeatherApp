package com.example.amira.openweatherapp.models;

public class DayWeather {
    private long Id;
    private Temperature Temperature;
    private Weather[] Weathers;
    private double Pressure;
    private double Humidity;
    private double Speed;
    private double Degree;
    private double Clouds;

    public void setId(long id) {
        Id = id;
    }

    public long getId() {
        return Id;
    }

    public void setTemperature(com.example.amira.openweatherapp.models.Temperature temperature) {
        Temperature = temperature;
    }

    public com.example.amira.openweatherapp.models.Temperature getTemperature() {
        return Temperature;
    }

    public void setClouds(double clouds) {
        Clouds = clouds;
    }

    public double getClouds() {
        return Clouds;
    }

    public void setDegree(double degree) {
        Degree = degree;
    }

    public double getDegree() {
        return Degree;
    }

    public void setHumidity(double humidity) {
        Humidity = humidity;
    }

    public double getHumidity() {
        return Humidity;
    }

    public void setPressure(double pressure) {
        Pressure = pressure;
    }

    public double getPressure() {
        return Pressure;
    }

    public void setSpeed(double speed) {
        Speed = speed;
    }

    public double getSpeed() {
        return Speed;
    }

    public void setWeathers(Weather[] weathers) {
        Weathers = weathers;
    }

    public Weather[] getWeathers() {
        return Weathers;
    }
}
