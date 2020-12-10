package com.example.myweatherapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class CurrentWeatherLoader extends AsyncTaskLoader<String> {

    private String myWeatherQueryString;
    private String myMeasurementType;

    public CurrentWeatherLoader(@NonNull Context context, String queryString, String measurementType) {
        super(context);
        myWeatherQueryString = queryString;
        myMeasurementType = measurementType;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        return NetworkUtils.getCurrentWeather(myWeatherQueryString, myMeasurementType);
    }
}
