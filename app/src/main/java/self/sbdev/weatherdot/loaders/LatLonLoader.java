package self.sbdev.weatherdot.loaders;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import self.sbdev.weatherdot.utils.NetworkUtils;

public class LatLonLoader extends AsyncTaskLoader<String> {

    //Member variables
    private String myCityQueryString;
    private String myMeasurementType;
    private String myLanguage;

    //AsyncTaskLoader constructor
    public LatLonLoader(@NonNull Context context, String queryString, String measurementType, String language) {
        super(context);
        myCityQueryString = queryString;
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
        return NetworkUtils.getLatLon(myCityQueryString, myMeasurementType, myLanguage);
    }
}
