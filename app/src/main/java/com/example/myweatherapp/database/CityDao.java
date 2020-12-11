package com.example.myweatherapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(City city);

    @Query("SELECT * FROM city_table ORDER BY city ASC")
    LiveData<List<City>> getAllCities();

    @Delete
    void deleteCity(City city);

    @Query("DELETE FROM city_table")
    void deleteAllCities();

    @Query("SELECT * FROM city_table")
    List<City> getListCities();
}
