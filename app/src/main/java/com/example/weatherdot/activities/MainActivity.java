package com.example.weatherdot.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.example.weatherdot.R;
import com.example.weatherdot.adapters.WeatherListAdapter;
import com.example.weatherdot.database.City;
import com.example.weatherdot.database.CityViewModel;
import com.example.weatherdot.loaders.LatLonLoader;
import com.example.weatherdot.loaders.WeatherLoader;
import com.example.weatherdot.objects.Weather;
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
import java.util.Locale;
import java.util.Set;

/**
 * This class displays current and daily weather information from
 * whatever city the user chooses to search for.
 * The app utilizes the OpenWeather Weather API to fetch weather information.
 * Currently, the user will have to be somewhat specific to which city
 * they want to search for (ex. searching "Rome" will fetch data for Rome, New York and not Rome, Italy).
 * In order to get Rome, Italy the user will have to type in "Rome,IT" into the input field.
 * Cities that are deemed to be Favorites of the user are saved in a Room database.
 * Users can 'favorite' a city by searching for it and tapping the 'Heart' icon
 * and 'un-favorite' a city by tapping the icon again.
 * Favorite cities and recently searched cities are
 * displayed in the Navigation Drawer for quick access.
 * The Options menu contains two buttons to clear the user's Favorite cities and recently searched ones.
 * Whenever a city is searched for, the current weather conditions are
 * displayed on the top part of the screen, which includes
 * a time-sensitive daytime indicator (blue for day, purple for night) and the corresponding condition icon.
 * In addition, daily weather conditions (up to the next 7 days)
 * are shown on the rest of the screen display through a RecyclerView.
 * Each card inside of the RecyclerView contains a corresponding condition icon,
 * a date, the weather condition, and that day's high & low temperatures.
 * The Navigation Drawer also includes access to a FAQ dialog, the app's settings
 * through the 'Settings' button, and a 'Send Feedback' button that directs
 * the user to their preferred email app and loads an email addressed to the developer's email.
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<String> {

    //Member variables
    private CityViewModel myCityViewModel;
    private RecyclerView myRecyclerView;
    private WeatherListAdapter myAdapter;
    private EditText myCityInput;
    private ImageView myWeatherBackground;
    private int initialBackgroundHeight;
    private TextView myCurrentTimeText;
    private ImageView myCurrentConditionIcon;
    private TextView myCurrentTempText;
    private TextView myCurrentFeelsLikeText;
    private TextView myCurrentConditionText;
    private ImageButton expandArrowButton;
    private ImageButton collapseArrowButton;
    private RelativeLayout extraInfoLayout;
    private TextView myCurrentHumidityText;
    private TextView myCurrentCloudinessText;
    private TextView myCurrentWindSpeedText;
    private TextView myCurrentUVIText;
    private ArrayList<Weather> myDailyWeatherData;
    private NavigationView navigationView;
    private ArrayList<String> recentLocList;
    private ToggleButton favoritesButton;
    private ArrayList<City> myCities;
    private Set<String> favCitiesSet;
    private String measurementPref;
    private static String languagePref;

    //AsyncTaskLoader IDs
    private static final int LAT_LON_LOADER = 1;
    private static final int WEATHER_LOADER = 2;

    /**
     * MainActivity's onCreate lifecycle state.
     * @param savedInstanceState Any instance state data that is saved.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing the toolbar and various views
        Toolbar toolbar = findViewById(R.id.toolbar);
        favoritesButton = findViewById(R.id.favorite_button);
        myCityInput = findViewById(R.id.search_field);
        myCityInput.setTextColor(getResources().getColor(R.color.white));
        myCityInput.setHintTextColor(getResources().getColor(R.color.white));
        myCityInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                //Determines what the 'Search' button on the keyboard does.
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //Tapping the button initiates the weather query and enables the favorites button (Heart icon).
                    getLatLon(textView);
                    textView.clearFocus();
                    favoritesButton.setEnabled(true);
                    favoritesButton.setChecked(false);
                    return true;
                }
                return false;
            }
        });
        myWeatherBackground = findViewById(R.id.current_weather_background);
        myWeatherBackground.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initialBackgroundHeight = myWeatherBackground.getHeight();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    myWeatherBackground.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                else {
                    myWeatherBackground.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
        myWeatherBackground.setVisibility(View.INVISIBLE);
        myCurrentTimeText = findViewById(R.id.current_time_text);
        myCurrentConditionIcon = findViewById(R.id.current_condition_icon);
        myCurrentTempText = findViewById(R.id.current_temp_text);
        myCurrentFeelsLikeText = findViewById(R.id.current_feels_like_text);
        myCurrentConditionText = findViewById(R.id.current_condition_text);
        expandArrowButton = findViewById(R.id.expand_button);
        collapseArrowButton = findViewById(R.id.collapse_button);
        extraInfoLayout = findViewById(R.id.extra_info_layout);
        myCurrentHumidityText = findViewById(R.id.humidity_text);
        myCurrentCloudinessText = findViewById(R.id.cloudiness_text);
        myCurrentWindSpeedText = findViewById(R.id.wind_speed_text);
        myCurrentUVIText = findViewById(R.id.uvi_text);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Initialize the Navigation Drawer
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

        //Initialize the ArrayList that contains the names of recently searched cities.
        recentLocList = new ArrayList<>();

        //Initiate AsyncTaskLoaders
        if(LoaderManager.getInstance(this).getLoader(LAT_LON_LOADER) != null) {
            LoaderManager.getInstance(this).initLoader(LAT_LON_LOADER, null, this);
        }
        if(LoaderManager.getInstance(this).getLoader(WEATHER_LOADER) != null) {
            LoaderManager.getInstance(this).initLoader(WEATHER_LOADER, null, this);
        }

        //Initialize the ViewModel that connects to the Room database.
        myCityViewModel = ViewModelProviders.of(this).get(CityViewModel.class);

        myCityViewModel.getAllCities().observe(this, new Observer<List<City>>() {
            @Override
            public void onChanged(List<City> cities) {
                myCities = (ArrayList<City>) cities;
            }
        });

        //Create a cached copy of the cities that are in the database.
        myCities = (ArrayList<City>) myCityViewModel.getListCities();

        //Populates the 'Favorites' section of the Navigation Drawer.
        createInitialFavorites(myCities);

        //Initialize a HashSet of favorite cities in order to check for duplicates.
        favCitiesSet = new HashSet<>();

        for(int i = 0; i < myCities.size(); i++) {
            favCitiesSet.add(myCities.get(i).getMyCity());
        }

        //Fetches the user's preferences from the Settings menu.
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        measurementPref = sharedPref.getString(SettingsActivity.KEY_PREF_MEASUREMENT, "fahrenheit");

        languagePref = sharedPref.getString(SettingsActivity.KEY_PREF_LANGUAGE, "en");

        if(myCities.size() != 0) {
            myCityInput.setText(myCities.get(0).getMyCity());
            favoritesButton.setEnabled(true);
            favoritesButton.setChecked(true);
            getLatLon(myCityInput);
        }
    }

    /**
     * Determines the actions of tapping their respective Navigation Drawer items.
     * @param item The tapped menu item.
     * @return A boolean that displays the item as the selected item.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        //Action for items within the 'Favorites' sub-menu.
        if(item.getItemId() == Menu.FIRST) {
            drawer.closeDrawer(GravityCompat.START);
            myCityInput.setText(item.getTitle().toString().trim());
            favoritesButton.setEnabled(true);
            favoritesButton.setChecked(true);
            getLatLon(myCityInput);
            return true;
        }

        //Action for items within the 'Recent Locations' sub-menu.
        if(item.getItemId() == Menu.NONE) {
            drawer.closeDrawer(GravityCompat.START);
            myCityInput.setText(item.getTitle().toString().trim());
            favoritesButton.setEnabled(true);
            favoritesButton.setChecked(false);
            getLatLon(myCityInput);
            return true;
        }

        if(item.getItemId() == R.id.nav_faq) {
            drawer.closeDrawer(GravityCompat.START);
            String faq1 = getString(R.string.faq1);
            String faq1a = getString(R.string.faq1a);
            String faq2 = getString(R.string.faq2);
            String faq2a1 = getString(R.string.faq2a1);
            String faq2a2 = getString(R.string.faq2a2);
            String faq2a3 = getString(R.string.faq2a3);
            String faq3 = getString(R.string.faq3);
            String faq3a1 = getString(R.string.faq3a1);
            String faq3a2 = getString(R.string.faq3a2);
            Spanned strMessage = Html.fromHtml(faq1 + "<br>" + faq1a + "<br><br>" + faq2 + "<br>" + faq2a1 + " " + faq2a2 + " " + faq2a3 + "<br><br>" + faq3 + "<br>" + faq3a1 + " " + faq3a2);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.faq_title)
                    .setMessage(strMessage)
                    .setPositiveButton(R.string.understood_button_text, null)
                    .show();
            return true;
        }

        //Action for the 'Settings' item within the 'Other' sub-menu.
        if(item.getItemId() == R.id.nav_settings) {
            drawer.closeDrawer(GravityCompat.START);
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        //Action for the 'Send Feedback' item within the 'Other' sub-menu.
        //Allows user to select their preferred email app and send an email to the developer.
        if(item.getItemId() == R.id.nav_send_feedback) {
            drawer.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"sbedeveloper@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "WeatherDot Feedback");
            if(intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                return true;
            }
        }
        return false;
    }

    /**
     * Required method that determines the actions when the option menu is created.
     * @param menu The respective options menu.
     * @return A boolean that determines if the menu is displayed or not.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the Options menu.
        getMenuInflater().inflate(R.menu.activity_main_options, menu);
        return true;
    }

    /**
     * Method that determines the actions of tapping their respective Option menu items.
     * @param item The tapped menu item.
     * @return A boolean that is consumed in this options menu (true) or forwarded to another event (false).
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.refresh) {
            getLatLon(myCityInput);
            return true;
        }

        if(id == R.id.clear_recent) {
            //Displays a Toast to the user relying their action.
            Toast.makeText(this, getString(R.string.rec_loc_cleared_toast_msg), Toast.LENGTH_LONG).show();

            //Fetching the 'Recent Locations' sub-menu.
            MenuItem recentLocItem = navigationView.getMenu().findItem(R.id.recent_locations);
            SubMenu subMenu = recentLocItem.getSubMenu();

            //Clears the cached ArrayList copy of recently searched cities.
            recentLocList.clear();
            //Clears the 'Recent Locations' sub-menu.
            subMenu.clear();
            return true;
        }

        if(id == R.id.clear_favorites) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.clear_favorites_action))
                    .setMessage(R.string.clear_favorites_confirm)
                    .setPositiveButton(R.string.yes_button_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Displays a Toast to the user relying their action.
                            Toast.makeText(MainActivity.this, getString(R.string.fav_cleared_toast_msg), Toast.LENGTH_LONG).show();

                            //Fetching the 'Favorites' sub-menu.
                            MenuItem favoriteLocItem = navigationView.getMenu().findItem(R.id.favorite_locations);
                            SubMenu subMenu = favoriteLocItem.getSubMenu();

                            //Clears the 'City' table within the Room database.
                            myCityViewModel.deleteAllCities();
                            favoritesButton.setChecked(false);
                            //Clears the cached ArrayList and HashSet copy of favorite cities.
                            myCities.clear();
                            favCitiesSet.clear();
                            //Clears the 'Favorites' sub-menu.
                            subMenu.clear();
                        }
                    })

                    .setNegativeButton(R.string.cancel_button_text, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method that initiates the search for current Weather using OpenWeather API.
     * @param view The input field where the user types in a city name.
     */
    public void getLatLon(View view) {
        //Get the search string from the input field.
        String queryString = myCityInput.getText().toString().trim();

        //Closes the keyboard after clicking the 'Search' button
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if(inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        //Checks the network connection of the device.
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if(connManager != null) {
            networkInfo = connManager.getActiveNetworkInfo();
        }

        //Checks to make sure that the network connection exists, that the network is connected,
        //and that the query string is available before commencing the search.
        if(networkInfo != null && networkInfo.isConnected() && queryString.length() != 0) {
            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", queryString);
            LoaderManager.getInstance(this).restartLoader(1, queryBundle, this);
        }
        //If there is no search term or no network connection, then these Toasts will show up as feedback to the user.
        else {
            if(queryString.length() == 0) {
                Toast.makeText(this, getString(R.string.no_city_toast_msg), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, getString(R.string.no_network_toast_msg), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Method that initiates the search for daily Weather using OpenWeather One Call API.
     * @param lat The latitude of the city that is being searched for.
     * @param lon The longitude of the city that is being searched for.
     */
    public void searchWeather(double lat, double lon) {
        //Checks the network connection of the device.
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if(connManager != null) {
            networkInfo = connManager.getActiveNetworkInfo();
        }

        //Checks to make sure that the network connection exists and is connected before commencing search.
        if(networkInfo != null && networkInfo.isConnected()) {
            Bundle queryBundle = new Bundle();
            queryBundle.putDouble("latitude", lat);
            queryBundle.putDouble("longitude", lon);
            LoaderManager.getInstance(this).restartLoader(2, queryBundle, this);
        }
    }

    /**
     * Required method for determining what the AsyncTaskLoader does when first created.
     * @param id The ID of the respective AsyncTaskLoader that was initialized.
     * @param args Any additional pieces of information that is needed to start the work of the AsyncTaskLoader.
     * @return The respective AsyncTaskLoader object.
     */
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        //Holder variables that will contain the user's preferences from the SettingsActivity.
        String measurementType = "";
        String chosenLanguage = "";

        if(id == LAT_LON_LOADER) {
            //The name of the city that is being searched for.
            String queryString = "";

            if(args != null) {
                queryString =  args.getString("queryString");
                measurementType = measurementPref;
                chosenLanguage = languagePref;
            }
            return new LatLonLoader(this, queryString, measurementType, chosenLanguage);
        }
        if(id == WEATHER_LOADER) {
            //The latitude and longitude of the city that is being searched for.
            double queryLat = 0;
            double queryLon = 0;

            if(args != null) {
                queryLat = args.getDouble("latitude");
                queryLon = args.getDouble("longitude");
                measurementType = measurementPref;
                chosenLanguage = languagePref;
            }
            return new WeatherLoader(this, queryLat, queryLon, measurementType, chosenLanguage);
        }
        return null;
    }

    /**
     * Required method for deciding what happens to the data fetched from the API call.
     * The JSON response is nit-picked in order to display only the wanted information.
     * @param loader The respective AsyncTaskLoader that was initialized.
     * @param data The JSON response from the API.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        //Shows Toast if there is no data to receive due to incorrect spelling of city name.
        if(data == null) {
            Toast.makeText(this, getString(R.string.invalid_city_toast_msg), Toast.LENGTH_LONG).show();
        }
        int id = loader.getId();
        if(id == LAT_LON_LOADER && data != null) {
            try{
                JSONObject jsonObject = new JSONObject(data);

                //Creates and adds a new menu item to the 'Recent Locations' sub-menu.
                //If the city is already in the sub-menu, a duplicate will not be made.

                Log.d("The set", favCitiesSet.toString());

                Log.d("check this", String.valueOf(favCitiesSet.contains(myCityInput.getText().toString())));
                if(!recentLocList.contains(jsonObject.getString("name")) && !favCitiesSet.contains(myCityInput.getText().toString())) {
                    MenuItem recentLocItem = navigationView.getMenu().findItem(R.id.recent_locations);
                    SubMenu subMenu = recentLocItem.getSubMenu();
                    subMenu.add(Menu.NONE, Menu.NONE, (800 - 5), capitalize(myCityInput.getText().toString()));
                    recentLocList.add(jsonObject.getString("name"));
                }

                //JSON access point for the coordinates.
                JSONObject coordObject = jsonObject.getJSONObject("coord");

                //Initializing latitude and longitude member variables that will hold the data gathered from the above access point.
                double coord_lat = 0;
                double coord_lon = 0;

                //Try to get latitude and longitude from data.
                //Catch if any field is empty and move on.
                try{
                    coord_lat = coordObject.getDouble("lat");
                    coord_lon = coordObject.getDouble("lon");
                }catch (Exception e) {
                    e.printStackTrace();
                }
                searchWeather(coord_lat, coord_lon);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(id == WEATHER_LOADER && data != null) {
            //Try to get (split between current and daily weather categories) deltaTime, timezone, temperatures, weather conditions, and condition icons from the data.
            //Also gets the extra details like probability of precipitation, humidity, cloudiness, wind speed, and UV index.
            //Catch if any field is empty and move on.
            try{
                JSONObject jsonDetailedObject = new JSONObject(data);
                //JSON Current access point
                JSONObject jsonCurrentObject = jsonDetailedObject.getJSONObject("current");
                JSONArray jsonCurrentWeatherObject = jsonCurrentObject.getJSONArray("weather");
                //JSON Daily access point
                JSONArray jsonDailyObject = jsonDetailedObject.getJSONArray("daily");

                //Initializing various member variables that will hold the data gathered from the JSON access points.
                int i = 0;
                String timeCurrent = null;
                String timezoneCurrent = null;
                String tempCurrent = null;
                String feelsLikeCurrent = null;
                String conditionCurrent = null;
                long dtCurrent;
                String iconCurrent = null;
                String humidityCurrent = null;
                String cloudinessCurrent = null;
                String windSpeedCurrent = null;
                String uviCurrent = null;
                long dtDaily;
                String dayDaily;
                String iconDaily;
                String condDaily;
                String tempsDaily;
                String precipDaily;
                String humidDaily;
                String cloudDaily;
                String windDaily;
                myDailyWeatherData = new ArrayList<>();
                String timezoneDaily = jsonDetailedObject.getString("timezone");

                try{
                    dtCurrent = jsonCurrentObject.getLong("dt");
                    timezoneCurrent = jsonDetailedObject.getString("timezone");
                    timeCurrent = getTime(dtCurrent, timezoneCurrent);
                    //Sets the color for the current weather display based on time of day.
                    if(isItDaytime(timeCurrent)) {
                        myWeatherBackground.setBackground(ContextCompat.getDrawable(this, R.drawable.gradient_day));
                    }
                    else {
                        myWeatherBackground.setBackground(ContextCompat.getDrawable(this, R.drawable.gradient_night));
                    }
                    myWeatherBackground.setVisibility(View.VISIBLE);
                    if(collapseArrowButton.getVisibility() == View.VISIBLE) {
                        expandArrowButton.setVisibility(View.INVISIBLE);
                    }
                    else {
                        expandArrowButton.setVisibility(View.VISIBLE);
                    }

                    iconCurrent = jsonCurrentWeatherObject.getJSONObject(0).getString("icon");
                    humidityCurrent = getString(R.string.humidity) + " " + jsonCurrentObject.getInt("humidity") + "%";
                    cloudinessCurrent = getString(R.string.cloudiness) + " " + jsonCurrentObject.getInt("clouds") + "%";
                    //Sets the temperature measurement type based on user's settings.
                    if(measurementPref.equalsIgnoreCase("celsius")) {
                        //Celsius
                        tempCurrent = "" + jsonCurrentObject.getInt("temp") + "\u2103";
                        feelsLikeCurrent = getString(R.string.feels_like) + " " + jsonCurrentObject.getInt("feels_like") + "\u2103";
                        windSpeedCurrent = getString(R.string.wind_speed) + " " + String.format(getLocale(), "%.2f", (jsonCurrentObject.getDouble("wind_speed") * 3.6)) + " Km/H";
                    }
                    else {
                        //Fahrenheit
                        tempCurrent = "" + jsonCurrentObject.getInt("temp") + "\u2109";
                        feelsLikeCurrent = getString(R.string.feels_like) + " " + jsonCurrentObject.getInt("feels_like") + "\u2109";
                        windSpeedCurrent = getString(R.string.wind_speed) + " " + String.format(getLocale(), "%.2f", jsonCurrentObject.getDouble("wind_speed")) + " Mph";
                    }

                    uviCurrent = getString(R.string.uv_index) + " " + jsonCurrentObject.getInt("uvi");
                    //Fetching the weather condition string.
                    conditionCurrent = capitalize(jsonCurrentWeatherObject.getJSONObject(0).getString("description"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                //If current temperature is found, set up the current weather display.
                //Glide is used to load the weather icon from the OpenWeather API.
                if(tempCurrent != null) {
                    myCurrentTimeText.setText(getString(R.string.most_recent_label, timeCurrent));
                    String iconURL = "https://openweathermap.org/img/wn/" + iconCurrent + ".png";
                    Glide.with(this).load(iconURL).override(250, 250).into(myCurrentConditionIcon);
                    myCurrentTempText.setText(tempCurrent);
                    myCurrentFeelsLikeText.setText(feelsLikeCurrent);
                    myCurrentConditionText.setText(conditionCurrent);
                    myCurrentHumidityText.setText(humidityCurrent);
                    myCurrentCloudinessText.setText(cloudinessCurrent);
                    myCurrentWindSpeedText.setText(windSpeedCurrent);
                    myCurrentUVIText.setText(uviCurrent);
                }

                //Traversing each item within the JSONArray.
                while(i < jsonDailyObject.length()) {
                    //Various JSON access points based on their label.
                    JSONObject daily = jsonDailyObject.getJSONObject(i);
                    JSONObject temp = daily.getJSONObject("temp");
                    JSONArray weatherArr = daily.getJSONArray("weather");
                    JSONObject weatherArrObject = weatherArr.getJSONObject(0);

                    try{
                        dtDaily = daily.getLong("dt");
                        dayDaily = getDate(dtDaily, timezoneDaily);
                        iconDaily = weatherArrObject.getString("icon");
                        condDaily = capitalize(weatherArrObject.getString("description"));
                        precipDaily = getString(R.string.precipitation) + " " + String.format(getLocale(), "%.0f", (daily.getDouble("pop") * 100)) + "%";
                        humidDaily = getString(R.string.humidity) + " " + daily.getInt("humidity") + "%";
                        cloudDaily = getString(R.string.cloudiness) + " " + daily.getInt("clouds") + "%";

                        if(measurementPref.equalsIgnoreCase("celsius")) {
                            //Celsius
                            tempsDaily = getString(R.string.temp_high) + temp.getInt("max") + "\u2103 \n" + getString(R.string.temp_low) + temp.getInt("min") + "\u2103";
                            windDaily = getString(R.string.wind_speed) + " " + String.format(getLocale(), "%.2f", (daily.getDouble("wind_speed") * 3.6)) + " Km/H";
                        }
                        else {
                            //Fahrenheit
                            tempsDaily = getString(R.string.temp_high) + temp.getInt("max") + "\u2109 \n" + getString(R.string.temp_low) + temp.getInt("min") + "\u2109";
                            windDaily = getString(R.string.wind_speed) + " " + String.format(getLocale(), "%.2f", daily.getDouble("wind_speed")) + " Mph";
                        }

                        //Adding to the arrayList that holds newly created Weather objects that are made from the recently fetched data.
                        //This ArrayList will be used with the RecyclerView adapter.
                        myDailyWeatherData.add(new Weather(dayDaily, iconDaily, condDaily, tempsDaily, precipDaily, humidDaily, cloudDaily, windDaily));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    i++;
                }
                //Initializing the RecyclerView
                myRecyclerView = findViewById(R.id.recyclerview);

                //Set the Layout Manager
                myRecyclerView.setLayoutManager(new LinearLayoutManager(this));

                //Initialize the adapter with the Weather data and set it to the RecyclerView.
                myAdapter = new WeatherListAdapter(this, myDailyWeatherData);
                ((SimpleItemAnimator) myRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                myRecyclerView.setAdapter(myAdapter);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Required method that isn't used.
     * @param loader The respective AsyncTaskLoader that was initialized.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    /**
     * The MainActivity's onStop lifecycle state.
     */
    @Override
    protected void onStop() {
        //On Activity Stop, both AsyncTaskLoaders will be destroyed
        //in order to prevent re-calling the Weather API.
        super.onStop();
        LoaderManager.getInstance(this).destroyLoader(LAT_LON_LOADER);
        LoaderManager.getInstance(this).destroyLoader(WEATHER_LOADER);
    }

    /**
     * The MainActivity's onRestart lifecycle state.
     */
    @Override
    protected void onRestart() {
        //On Activity Restart, any changed settings will be applied upon the next search.
        super.onRestart();
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        measurementPref = sharedPref.getString(SettingsActivity.KEY_PREF_MEASUREMENT, "fahrenheit");

        languagePref = sharedPref.getString(SettingsActivity.KEY_PREF_LANGUAGE, "en");

        SharedPreferences.Editor preferencesEditor = sharedPref.edit();
        preferencesEditor.putString(SettingsActivity.KEY_PREF_MEASUREMENT, measurementPref);
        preferencesEditor.putString(SettingsActivity.KEY_PREF_LANGUAGE, languagePref);
        preferencesEditor.apply();
    }

    /**
     * Utilizes the fetched deltaTime (in UNIX Epoch time format) and timezone to create a
     * readable date string to be displayed.
     * Pattern "EEEE, MMM d" outputs as "dayOfTheWeek, abbreviated month, numberOfDayInMonth (#)",
     * which is then converted to a different language depending on the Locale used.
     * @param dt Time in seconds since 01-01-1970 which gets converted to current time.
     * @param timezone A String that contains the name of the timezone.
     * @return A String that is formatted as a readable date.
     */
    public String getDate(long dt, String timezone) {
        Date date = new java.util.Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d", getLocale());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone(timezone));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    /**
     * Utilizes the fetched deltaTime (in UNIX Epoch time format) and timezone offset (from UTC)
     * to create a readable Time string to be displayed.
     * The timezone offset is explicitly used to get the correct timezone that the city is based in.
     * Pattern "h:mm a, z" outputs as "hour:minutes AM/PM, timezone",
     * which is then converted to a different language depending on the Locale used.
     * @param dt Time in seconds since 01-01-1970 which gets converted to current time.
     * @param timezone A String that contains the name of the timezone.
     * @return A String that is formatted as a readable time statement.
     */
    public String getTime(long dt, String timezone) {
        Date time = new java.util.Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, z", getLocale());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone(timezone));
        String formattedTime = sdf.format(time);
        return formattedTime;
    }

    /**
     * Method that determines if a time statement is daytime or not based on a specified time range (6 AM to 6 PM).
     * If the time is inside of this range, then it is considered "daytime" and returns true,
     * else it is "nighttime" and returns false.
     * @param currentTime The resulting String from the getTime method.
     * @return True if "daytime", false if "nighttime".
     */
    public static boolean isItDaytime(String currentTime) {
        boolean isDaytime = false;
        try {
            Date time1 = new SimpleDateFormat("HH:mm", getLocale()).parse("06:30");

            Date time2 = new SimpleDateFormat("HH:mm", getLocale()).parse("18:30");

            Date timeCurrent = new SimpleDateFormat("h:mm a", getLocale()).parse(currentTime);

            //If current time is AFTER 6:30 AM and BEFORE 6:30 PM
            if(timeCurrent.after(time1) && timeCurrent.before(time2)) {
                isDaytime = true;
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return isDaytime;
    }

    /**
     * Method that determines the correct Locale for language preference purposes.
     * This is used in the getDate, getTime, and isItDaytime methods in order to
     * get the right translations based on the language preference.
     * @return The newly determined Locale based on the user's language preference.
     */
    public static Locale getLocale() {
        Locale loc = Locale.ENGLISH;
        switch (languagePref) {
            case "en":
                loc = Locale.ENGLISH;
                break;
            case "es":
                loc = new Locale("es");
                break;
            case "fr":
                loc = Locale.FRENCH;
                break;
            case "de":
                loc = new Locale("de");
                break;
            case "zh_cn":
                loc = Locale.SIMPLIFIED_CHINESE;
                break;
            case "ja":
                loc = Locale.JAPANESE;
                break;
            case "kr":
                loc = Locale.KOREAN;
        }
        return loc;
    }

    /**
     * Method that capitalizes the inputted String.
     * This is mainly used for the city names inside of the Navigation Drawer.
     * @param input Any String, but for the purpose of this app it is the city's name.
     * @return A String with all words having their first letter capitalized.
     */
    public static String capitalize(String input) {
        String[] words = input.toLowerCase().split(" ");
        StringBuilder builder = new StringBuilder();
        for(String s : words) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");
        }
        return builder.toString();
    }

    /**
     * Method that sets/removes a searched city as a favorite once the user taps on the Heart icon.
     * One tap will mark it as a favorite, another will un-mark it as a favorite.
     * When doing this, a new menu item is created under the 'Favorites' sub-menu in the Navigation Drawer,
     * and if unfavorited, will remove that menu item.
     * In the background, the city will be inserted or deleted from the Room database.
     * @param view The Heart icon (ToggleButton) next to the city input field.
     */
    public void setFavorite(View view) {
        boolean checked = ((ToggleButton) view).isChecked();
        String searchedCity = myCityInput.getText().toString();

        MenuItem favoriteLocItem = navigationView.getMenu().findItem(R.id.favorite_locations);
        SubMenu subMenu = favoriteLocItem.getSubMenu();

        for(int i = 0; i < myCities.size(); i++) {
            //Populate the HashSet with favorite cities in order to check if it already exists.
            favCitiesSet.add(myCities.get(i).getMyCity());
        }
        if(checked && !favCitiesSet.contains(searchedCity) && searchedCity.trim().length() > 0) {
            myCityViewModel.insert(new City(searchedCity));
            favCitiesSet.add(searchedCity);

            //Adds the new favorite city to 'Favorites' section, while also removing it from 'Recent Locations' section.
            subMenu.add(Menu.NONE, Menu.NONE, (800 - 5), searchedCity);

            for(int i = 0; i < recentLocList.size(); i++) {
                if(recentLocList.contains(searchedCity)) {
                    recentLocList.remove(searchedCity);
                }
            }

            recreateRecentLocations();

            Toast.makeText(this, "\"" + searchedCity + "\"" + getString(R.string.added_to_fav_toast_msg), Toast.LENGTH_LONG).show();
        }
        else if(checked && favCitiesSet.contains(searchedCity)) {
            //If a favorite city is manually searched for and is already a favorite,
            //this message is displayed if the user tries to favorite it again.
            Toast.makeText(this, getString(R.string.already_fav_toast_msg), Toast.LENGTH_LONG).show();
        }
        if(!checked && favCitiesSet.contains(searchedCity)) {
            for(int j = 0; j < myCities.size(); j++) {
                if(searchedCity.equals(myCities.get(j).getMyCity())) {
                    myCityViewModel.deleteCity(myCities.get(j));
                    favCitiesSet.remove(myCities.get(j).getMyCity());
                    myCities.remove(j);

                    Toast.makeText(this, "\"" + searchedCity + "\"" + getString(R.string.delete_from_fav_toast_msg), Toast.LENGTH_SHORT).show();

                    //Clears the 'Favorites' sub-menu and re-creates it with the city removed.
                    subMenu.clear();

                    createInitialFavorites(myCities);

                    //Takes the city and adds it back into the 'Recent Locations' section.
                    recentLocList.add(searchedCity);
                    recreateRecentLocations();
                }
            }
        }
    }

    /**
     * Method is used to create the 'Favorites' sub-menu in the Navigation Drawer when onCreate is called.
     * @param citiesList The ArrayList cached copy of the cities that are in the database.
     */
    public void createInitialFavorites(ArrayList<City> citiesList) {
        MenuItem favoriteLocItem = navigationView.getMenu().findItem(R.id.favorite_locations);
        SubMenu subMenu = favoriteLocItem.getSubMenu();
        for(int i = 0; i < citiesList.size(); i++) {
            subMenu.add(Menu.FIRST, Menu.FIRST, (800 - 5), citiesList.get(i).getMyCity());
        }
    }

    /**
     * Method is ued to re-create the 'Recent Locations' sub-menu in the Navigation Drawer.
     */
    public void recreateRecentLocations() {
        MenuItem recentLocItem = navigationView.getMenu().findItem(R.id.recent_locations);
        SubMenu subMenu = recentLocItem.getSubMenu();

        subMenu.clear();
        for(int i = 0 ; i < recentLocList.size(); i++) {
            subMenu.add(Menu.NONE, Menu.NONE, (800 - 5), recentLocList.get(i));
        }
    }

    /**
     * Wrapper method to initiate the expansion of the WeatherBackground view.
     * @param view The expand button that is clicked in order to expand the WeatherBackground view.
     */
    public void showExtraInfo(View view) {
        expandLayout(myWeatherBackground, 500, myWeatherBackground.getHeight(), 1000);
        view.setVisibility(View.INVISIBLE);
        collapseArrowButton.setVisibility(View.VISIBLE);
        collapseArrowButton.setEnabled(false);
        extraInfoLayout.setVisibility(View.VISIBLE);

        //Adds a delay between button clicks in order to prevent extraInfoLayout from breaking.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                collapseArrowButton.setEnabled(true);
            }
        }, 650);
    }

    /**
     * Method that expands the WeatherBackground view height-wise using ValueAnimator.
     * @param view The WeatherBackground view
     * @param duration The amount of time it takes in milliseconds to complete the expanding animation.
     * @param prevHeight The original height value of the WeatherBackground.
     * @param targetHeight The height value that the WeatherBackground should be increased to.
     */
    public void expandLayout(final View view, int duration, int prevHeight, int targetHeight) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                view.getLayoutParams().height = (int) valueAnimator.getAnimatedValue();
                view.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    /**
     * Wrapper method to initiate the collapse of the WeatherBackground view.
     * @param view The collapse button that is clicked in order to shrink the WeatherBackground view.
     */
    public void hideExtraInfo(View view) {
        collapseLayout(myWeatherBackground, 500, initialBackgroundHeight);
        view.setVisibility(View.INVISIBLE);
        expandArrowButton.setVisibility(View.VISIBLE);
        expandArrowButton.setEnabled(false);

        //Adds a delay between button clicks in order to prevent extraInfoLayout from breaking.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                expandArrowButton.setEnabled(true);
            }
        }, 650);
    }

    /**
     * Method that shrinks the WeatherBackground view height-wise using ValueAnimator.
     * @param view The WeatherBackground view
     * @param duration The amount of time it takes in milliseconds to complete the expanding animation.
     * @param targetHeight The original height value of the WeatherBackground prior to expansion.
     */
    public void collapseLayout(final View view, int duration, int targetHeight) {
        int prevHeight = view.getHeight();

        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                view.getLayoutParams().height = (int) valueAnimator.getAnimatedValue();
                view.requestLayout();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                extraInfoLayout.setVisibility(View.INVISIBLE);
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }
}