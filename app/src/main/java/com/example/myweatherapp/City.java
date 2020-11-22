package com.example.myweatherapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "city_table")
public class City {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "city")
    private String myCity;

    public City(@NonNull String myCity) {
        this.myCity = myCity;
    }

    @Ignore
    public City(int id, @NonNull String myCity) {
        this.id = id;
        this.myCity = myCity;
    }

    public String getMyCity() {
        return this.myCity;
    }

    public void setMyCity(@NonNull String myCity) {
        this.myCity = myCity;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
}