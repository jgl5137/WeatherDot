package self.sbdev.weatherdot.loaders;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import self.sbdev.weatherdot.utils.NetworkUtils;

public class WeatherLoader extends AsyncTaskLoader<String> {

    //Member variables
    private double myQueryLat;
    private double myQueryLon;
    private String myMeasurementType;
    private String myLanguage;

    //AsyncTaskLoader constructor
    public WeatherLoader(@NonNull Context context, double lat, double lon, String measurementType, String language) {
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
        return NetworkUtils.getWeather(myQueryLat, myQueryLon, myMeasurementType, myLanguage);
    }
}
