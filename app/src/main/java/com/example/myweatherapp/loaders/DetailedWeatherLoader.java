package com.example.myweatherapp.loaders;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.example.myweatherapp.utils.NetworkUtils;

public class DetailedWeatherLoader extends AsyncTaskLoader<String> {

    //Member variables
    private double myQueryLat;
    private double myQueryLon;
    private String myMeasurementType;
    private String myLanguage;

    //AsyncTaskLoader constructor
    public DetailedWeatherLoader(@NonNull Context context, double lat, double lon, String measurementType, String language) {
        super(context);
        myQueryLat = lat;
        myQueryLon = lon;
        myMeasurementType = measurementType;
        myLanguage = language;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        return NetworkUtils.getDailyWeather(myQueryLat, myQueryLon, myMeasurementType, myLanguage);
    }
}
