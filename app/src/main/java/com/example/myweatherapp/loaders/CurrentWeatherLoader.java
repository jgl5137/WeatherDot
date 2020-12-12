package com.example.myweatherapp.loaders;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.example.myweatherapp.utils.NetworkUtils;

public class CurrentWeatherLoader extends AsyncTaskLoader<String> {

    //Member variables
    private String myWeatherQueryString;
    private String myMeasurementType;
    private String myLanguage;

    //AsyncTaskLoader constructor
    public CurrentWeatherLoader(@NonNull Context context, String queryString, String measurementType, String language) {
        super(context);
        myWeatherQueryString = queryString;
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
        return NetworkUtils.getCurrentWeather(myWeatherQueryString, myMeasurementType, myLanguage);
    }
}
