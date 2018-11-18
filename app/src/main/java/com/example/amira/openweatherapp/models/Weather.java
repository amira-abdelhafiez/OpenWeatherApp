package com.example.amira.openweatherapp.models;

public class Weather {
    private int Id;
    private String Main;
    private String Description;

    public void setId(int id) {
        Id = id;
    }

    public int getId() {
        return Id;
    }

    public void setMain(String main) {
        Main = main;
    }

    public String getMain() {
        return Main;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDescription() {
        return Description;
    }
}
