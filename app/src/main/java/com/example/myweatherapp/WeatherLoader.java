package com.example.myweatherapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class WeatherLoader extends AsyncTaskLoader<String> {

    private String myWeatherQueryString;

    public WeatherLoader(@NonNull Context context, String queryString) {
        super(context);
        myWeatherQueryString = queryString;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        return NetworkUtils.getCurrentWeather(myWeatherQueryString);
    }
}
