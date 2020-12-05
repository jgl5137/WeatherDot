package com.example.myweatherapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<String> {
    private CityViewModel myCityViewModel;
    private RecyclerView myRecyclerView;
    private WeatherListAdapter myAdapter;
    private EditText myWeatherInput;
    private ImageView myCurrentWeatherDisplay;
    private TextView myCurrentTimeDisplay;
    private TextView myCurrentTempDisplay;
    private ArrayList<Weather> myWeatherData;
    private NavigationView navigationView;
    private ArrayList<String> recentLocList;
    private ToggleButton favoritesButton;
    private ArrayList<City> myCities;
    private Set<String> favCitiesSet;

    private static final int CURRENT_WEATHER_LOADER = 1;
    private static final int DETAILED_WEATHER_LOADER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        favoritesButton = findViewById(R.id.favorite_button);
        myWeatherInput = findViewById(R.id.search_field);
        myWeatherInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchWeather(textView);
                    textView.clearFocus();
                    favoritesButton.setEnabled(true);
                    favoritesButton.setChecked(false);
                    return true;
                }
                return false;
            }
        });
        myCurrentWeatherDisplay = findViewById(R.id.current_weather_display);
        myCurrentWeatherDisplay.setVisibility(View.INVISIBLE);
        myCurrentTimeDisplay = findViewById(R.id.current_time_display);
        myCurrentTempDisplay = findViewById(R.id.current_temp_display);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if(drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);

        if(navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        recentLocList = new ArrayList<String>();

        if(LoaderManager.getInstance(this).getLoader(1) != null) {
            LoaderManager.getInstance(this).initLoader(1, null, this);
        }
        if(LoaderManager.getInstance(this).getLoader(2) != null) {
            LoaderManager.getInstance(this).initLoader(2, null, this);
        }

        myCityViewModel = ViewModelProviders.of(this).get(CityViewModel.class);

//        getApplicationContext().deleteDatabase("city_database");

        myCityViewModel.getAllCities().observe(this, new Observer<List<City>>() {
            @Override
            public void onChanged(List<City> cities) {
                myCities = (ArrayList<City>) cities;
            }
        });

        myCities = (ArrayList<City>) myCityViewModel.getListCities();

        createInitialFavorites(myCities);

        favCitiesSet = new HashSet<String>();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if(item.getItemId() == Menu.NONE) {
            drawer.closeDrawer(GravityCompat.START);
            myWeatherInput.setText(item.getTitle());
            favoritesButton.setEnabled(true);
            searchWeather(myWeatherInput);
            return true;
        }

        if(item.getItemId() == Menu.FIRST) {
            drawer.closeDrawer(GravityCompat.START);
            myWeatherInput.setText(item.getTitle());
            favoritesButton.setEnabled(true);
            favoritesButton.setChecked(true);
            searchWeather(myWeatherInput);
            return true;
        }

        if(item.getItemId() == R.id.nav_send_feedback) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"Jlau219@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Weather App Feedback");
            if(intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.clear_favorites) {
            Toast.makeText(this, "Your Favorites have been cleared!", Toast.LENGTH_LONG).show();

            MenuItem favoriteLocItem = navigationView.getMenu().findItem(R.id.favorite_locations);
            SubMenu subMenu = favoriteLocItem.getSubMenu();

            myCityViewModel.deleteAllCities();
            favoritesButton.setChecked(false);
            myCities.clear();
            subMenu.clear();
            return true;
        }

        if(id == R.id.clear_recent) {
            Toast.makeText(this, "Recent Locations have been cleared!", Toast.LENGTH_LONG).show();

            MenuItem recentLocItem = navigationView.getMenu().findItem(R.id.recent_locations);
            SubMenu subMenu = recentLocItem.getSubMenu();

            recentLocList.clear();
            subMenu.clear();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        else {
            if(queryString.length() == 0) {
                Toast.makeText(this, "No city name detected. Please input a valid city name.", Toast.LENGTH_LONG).show();
            }
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
        if(data == null) {
            Toast.makeText(this, "Invalid city name. Please check your spelling and input a valid city name.", Toast.LENGTH_LONG).show();
        }
        int id = loader.getId();
        if(id == CURRENT_WEATHER_LOADER && data != null) {
            try{
                JSONObject jsonObject = new JSONObject(data);

                if(!recentLocList.contains(jsonObject.getString("name"))) {
                    MenuItem recentLocItem = navigationView.getMenu().findItem(R.id.recent_locations);
                    SubMenu subMenu = recentLocItem.getSubMenu();
                    subMenu.add(Menu.NONE, Menu.NONE, (800 - 5), jsonObject.getString("name"));
                    recentLocList.add(jsonObject.getString("name"));
                }

                JSONObject coordObject = jsonObject.getJSONObject("coord");
                JSONArray weatherDescObject = jsonObject.getJSONArray("weather");
                JSONObject mainObject = jsonObject.getJSONObject("main");
                int i = 0;
                String time = null;
                String condition = null;
                String temp_Current = null;
                double coord_lat = 0;
                double coord_lon = 0;
                long dt_current = jsonObject.getLong("dt");
                int timezoneOffset = jsonObject.getInt("timezone");

                try{
                    time = getTime(dt_current, timezoneOffset);
                    if(isItDaytime(time)) {
                        myCurrentWeatherDisplay.setBackground(ContextCompat.getDrawable(this, R.drawable.gradient_day));
                        myCurrentWeatherDisplay.setVisibility(View.VISIBLE);
                    }
                    else {
                        myCurrentWeatherDisplay.setBackground(ContextCompat.getDrawable(this, R.drawable.gradient_night));
                        myCurrentWeatherDisplay.setVisibility(View.VISIBLE);
                    }
                    temp_Current = "" + mainObject.getInt("temp") + "\u2109";
                    coord_lat = coordObject.getDouble("lat");
                    coord_lon = coordObject.getDouble("lon");
                }catch (Exception e) {
                    e.printStackTrace();
                }
                if(temp_Current != null) {
                    myCurrentTimeDisplay.setText("Most Recently: " + time);
                    myCurrentTempDisplay.setText(temp_Current);
                    searchDetailedWeather(coord_lat, coord_lon);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(id == DETAILED_WEATHER_LOADER && data != null) {
            try{
                JSONObject jsonDetailedObject = new JSONObject(data);
                JSONArray jsonDailyObject = jsonDetailedObject.getJSONArray("daily");

                int i = 0;
                long dt;
                String day;
                String icon;
                String cond;
                String temps;
                myWeatherData = new ArrayList<Weather>();
                String timezone = jsonDetailedObject.getString("timezone");

                while(i < jsonDailyObject.length()) {
                    JSONObject daily = jsonDailyObject.getJSONObject(i);
                    JSONObject temp = daily.getJSONObject("temp");
                    JSONArray weatherArr = daily.getJSONArray("weather");
                    JSONObject weatherArrObject = weatherArr.getJSONObject(0);

                    try{
                        dt = daily.getLong("dt");
                        day = getDate(dt, timezone);
                        icon = weatherArrObject.getString("icon");
                        cond = capitalize(weatherArrObject.getString("description"));
                        temps = "High: " + temp.getInt("max") + "\u2109 \n" + "Low: " + temp.getInt("min") + "\u2109";
                        myWeatherData.add(new Weather(day, icon, cond, temps));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    i++;
                }
                myRecyclerView = findViewById(R.id.recyclerview);
                myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                myAdapter = new WeatherListAdapter(this, myWeatherData);
                myRecyclerView.setAdapter(myAdapter);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    public String getDate(long dt, String timezone) {
        Date date = new java.util.Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone(timezone));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public String getTime(long dt, int timezoneOffset) {
        timezoneOffset = (timezoneOffset * 1000);
        String[] availableIDs = TimeZone.getAvailableIDs(timezoneOffset);

        Date time = new java.util.Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, z");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone(availableIDs[0]));
        String formattedTime = sdf.format(time);
        return formattedTime;
    }

    public static boolean isItDaytime(String currentTime) {

        System.out.println(currentTime);
        boolean isDaytime = false;
        try {
            Date time1 = new SimpleDateFormat("HH:mm").parse("07:00");

            Date time2 = new SimpleDateFormat("HH:mm").parse("19:00");

            Date timeCurrent = new SimpleDateFormat("h:mm a").parse(currentTime);

            if(timeCurrent.after(time1) && timeCurrent.before(time2)) {
                isDaytime = true;
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return isDaytime;
    }

    public static String capitalize(String input) {
        String[] words = input.toLowerCase().split(" ");
        StringBuilder builder = new StringBuilder();
        for(String s : words) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");
        }
        return builder.toString();
    }

    public void setFavorite(View view) {
        boolean checked = ((ToggleButton) view).isChecked();
        String searchedCity = myWeatherInput.getText().toString();

        MenuItem favoriteLocItem = navigationView.getMenu().findItem(R.id.favorite_locations);
        SubMenu subMenu = favoriteLocItem.getSubMenu();

        for(int i = 0; i < myCities.size(); i++) {
            favCitiesSet.add(myCities.get(i).getMyCity());
        }
        if(checked && !favCitiesSet.contains(searchedCity) && searchedCity.trim().length() > 0) {
            myCityViewModel.insert(new City(searchedCity));

            subMenu.add(Menu.NONE, Menu.NONE, (800 - 5), searchedCity);

            Toast.makeText(this, "\"" + searchedCity + "\"" + " has been added to Favorites!", Toast.LENGTH_SHORT).show();
        }
        if(checked && favCitiesSet.contains(searchedCity)) {
            Toast.makeText(this, "This location is already a favorite!", Toast.LENGTH_LONG).show();
        }
        if(!checked && favCitiesSet.contains(searchedCity)) {
            for(int j = 0; j < myCities.size(); j++) {
                if(searchedCity.equals(myCities.get(j).getMyCity())) {
                    myCityViewModel.deleteCity(myCities.get(j));
                    favCitiesSet.remove(myCities.get(j).getMyCity());
                    myCities.remove(j);

                    Toast.makeText(this, "\"" + searchedCity + "\"" + " has been removed from Favorites!", Toast.LENGTH_SHORT).show();

                    subMenu.clear();

                    createInitialFavorites(myCities);
                }
            }
        }
    }

    public void createInitialFavorites(ArrayList<City> citiesList) {
        MenuItem favoriteLocItem = navigationView.getMenu().findItem(R.id.favorite_locations);
        SubMenu subMenu = favoriteLocItem.getSubMenu();
        for(int i = 0; i < citiesList.size(); i++) {
            subMenu.add(Menu.FIRST, Menu.FIRST, (800 - 5), citiesList.get(i).getMyCity());
        }
    }
}