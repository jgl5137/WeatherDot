package com.example.myweatherapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<String> {
    private EditText myWeatherInput;
    private TextView myDayText;
    private TextView myConditionText;
    private TextView myHighLowText;
    private TextView myCurrentTempDisplay;

    private static final int CURRENT_WEATHER_LOADER = 1;
    private static final int DETAILED_WEATHER_LOADER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        myWeatherInput = findViewById(R.id.search_field);
        myDayText = findViewById(R.id.day_field);
        myConditionText = findViewById(R.id.condition_field);
        myHighLowText = findViewById(R.id.temp_high_low);
        myCurrentTempDisplay = findViewById(R.id.current_Temp_Display);

        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if(drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        if(navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        if(LoaderManager.getInstance(this).getLoader(1) != null) {
            LoaderManager.getInstance(this).initLoader(1, null, this);
        }
        if(LoaderManager.getInstance(this).getLoader(2) != null) {
            LoaderManager.getInstance(this).initLoader(2, null, this);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    public void searchWeather(View view) {
        String queryString = myWeatherInput.getText().toString();

        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if(inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if(connManager != null) {
            networkInfo = connManager.getActiveNetworkInfo();
        }

        if(networkInfo != null && networkInfo.isConnected() && queryString.length() != 0) {
            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", queryString);
            LoaderManager.getInstance(this).restartLoader(1, queryBundle, this);
        }
    }

    public void searchDetailedWeather(int lat, int lon) {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if(connManager != null) {
            networkInfo = connManager.getActiveNetworkInfo();
        }

        if(networkInfo != null && networkInfo.isConnected()) {
            Bundle queryBundle = new Bundle();
            queryBundle.putInt("latitude", lat);
            queryBundle.putInt("longitude", lon);
            LoaderManager.getInstance(this).restartLoader(2, queryBundle, this);
        }
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        if(id == CURRENT_WEATHER_LOADER) {
            String queryString = "";

            if(args != null) {
                queryString =  args.getString("queryString");
            }
            return new CurrentWeatherLoader(this, queryString);
        }
        if(id == DETAILED_WEATHER_LOADER) {
            int queryLat = 0;
            int queryLon = 0;

            if(args != null) {
                queryLat = args.getInt("latitude");
                queryLon = args.getInt("longitude");
            }
            return new DetailedWeatherLoader(this, queryLat, queryLon);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        int id = loader.getId();
        if(id == CURRENT_WEATHER_LOADER) {
            try{
                JSONObject jsonObject = new JSONObject(data);
                JSONObject coordObject = jsonObject.getJSONObject("coord");
                JSONObject weatherDescObject = jsonObject.getJSONObject("weather");
                JSONObject mainObject = jsonObject.getJSONObject("main");
                int i = 0;
                String day = null;
                String condition = null;
                String temp_Current = null;
                String temp_High = null;
                String temp_Low = null;

                try{
                    temp_Current = mainObject.getString("temp");
                }catch (Exception e) {
                    e.printStackTrace();
                }
                if(temp_Current != null) {
                    myCurrentTempDisplay.setText(temp_Current);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}