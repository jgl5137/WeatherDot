package com.example.myweatherapp;

import androidx.room.Entity;

@Entity(tableName = "weather_table")
public class Weather {

    private String city;
    private int lon;
    private int lat;
    private String description;
    private int temp;
    private int temp_min_high;
}
