package com.example.myweatherapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class DetailedWeatherLoader extends AsyncTaskLoader<String> {

    private double myQueryLat;
    private double myQueryLon;

    public DetailedWeatherLoader(@NonNull Context context, double lat, double lon) {
        super(context);
        myQueryLat = lat;
        myQueryLon = lon;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        return NetworkUtils.getDetailedWeather(myQueryLat, myQueryLon);
    }
}
