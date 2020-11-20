package com.example.myweatherapp;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class CityViewModel extends AndroidViewModel {
    private CityRepository myRepository;
    private LiveData<List<City>> myAllCities;

    public CityViewModel(Application application) {
        super(application);
        myRepository = new CityRepository(application);
        myAllCities = myRepository.getAllCities();
    }

    LiveData<List<City>> getAllCities() {
        return myAllCities;
    }

    public void insert(City city) {
        myRepository.insert(city);
    }

    public void deleteCity(City city) {
        myRepository.deleteCity(city);
    }
}
