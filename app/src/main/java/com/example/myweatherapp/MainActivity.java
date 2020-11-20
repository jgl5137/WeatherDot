package com.example.myweatherapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<String> {
    private CityViewModel myCityViewModel;
    private RecyclerView myRecyclerView;
    private WeatherListAdapter myAdapter;
    private EditText myWeatherInput;
    private TextView myCurrentTempDisplay;

    private static final int CURRENT_WEATHER_LOADER = 1;
    private static final int DETAILED_WEATHER_LOADER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        myWeatherInput = findViewById(R.id.search_field);
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

        //myRecyclerView = findViewById(R.id.recyclerview);

        myCityViewModel = ViewModelProviders.of(this).get(CityViewModel.class);

        myCityViewModel.insert(new City("Pittsburgh"));

        myCityViewModel.getAllCities().observe(this, new Observer<List<City>>() {
            @Override
            public void onChanged(List<City> cities) {

            }
        });
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

    public void searchDetailedWeather(double lat, double lon) {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if(connManager != null) {
            networkInfo = connManager.getActiveNetworkInfo();
        }

        if(networkInfo != null && networkInfo.isConnected()) {
            Bundle queryBundle = new Bundle();
            queryBundle.putDouble("latitude", lat);
            queryBundle.putDouble("longitude", lon);
            LoaderManager.getInstance(this).restartLoader(2, queryBundle, this);
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        if(id == CURRENT_WEATHER_LOADER) {
            String queryString = "";

            if(args != null) {
                queryString =  args.getString("queryString");
            }
            return new CurrentWeatherLoader(this, queryString);
        }
        if(id == DETAILED_WEATHER_LOADER) {
            double queryLat = 0;
            double queryLon = 0;

            if(args != null) {
                queryLat = args.getDouble("latitude");
                queryLon = args.getDouble("longitude");
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
                JSONArray weatherDescObject = jsonObject.getJSONArray("weather");
                JSONObject mainObject = jsonObject.getJSONObject("main");
                int i = 0;
                String day = null;
                String condition = null;
                String temp_Current = null;
                String temp_High = null;
                String temp_Low = null;
                double coord_lat = 0;
                double coord_lon = 0;

                try{
                    temp_Current = mainObject.getString("temp");
                    coord_lat = coordObject.getDouble("lat");
                    coord_lon = coordObject.getDouble("lon");
                }catch (Exception e) {
                    e.printStackTrace();
                }
                if(temp_Current != null) {
                    myCurrentTempDisplay.setText(temp_Current);
                    searchDetailedWeather(coord_lat, coord_lon);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(id == DETAILED_WEATHER_LOADER) {
            try{
                JSONObject jsonDetailedObject = new JSONObject(data);
                JSONArray jsonDailyObject = jsonDetailedObject.getJSONArray("daily");

                int i = 0;
                String day = null;
                String cond = null;
                String temps = null;

                while(i < jsonDailyObject.length()) {
                    JSONObject daily = jsonDailyObject.getJSONObject(i);
                    JSONObject temp = daily.getJSONObject("temp");

                    try{
                        day = daily.getString("dt");
                        temps = temp.getString("max") + " " + temp.getString("min");
                        cond = "Cloudy";

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    i++;
                }


            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}