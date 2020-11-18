package com.example.myweatherapp;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class NetworkUtils {
    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    private static final String API_KEY = "e83f7c0a2b6d6c8316fea85fb334134c";
    private static final String CURRENT_OPEN_WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?";
    private static final String QUERY_PARAM = "q";
    private static final String UNITS = "units";
    private static final String APP_ID = "appid";
    private static final String LANG = "lang";
    private static final String DETAILED_OPEN_WEATHER_URL = "https://api.openweathermap.org/data/2.5/onecall?";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String EXCLUDE = "exclude";

    static String getCurrentWeather(String queryString) {
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        String currentWeatherJSONString = null;

        try{
            Uri builtURI = Uri.parse(CURRENT_OPEN_WEATHER_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, queryString)
                    .appendQueryParameter(UNITS, "imperial")
                    .appendQueryParameter(LANG, "en")
                    .appendQueryParameter(APP_ID, API_KEY)
                    .build();

            URL requestURL = new URL(builtURI.toString());

            urlConnection = (HttpsURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder builder = new StringBuilder();

            String newLine;

            while ((newLine = reader.readLine()) != null) {
                builder.append(newLine);
                builder.append("\n");
            }

            if(builder.length() == 0) {
                return null;
            }

            currentWeatherJSONString = builder.toString();

            Log.d(LOG_TAG, currentWeatherJSONString);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
            if(reader != null) {
                try{
                    reader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return currentWeatherJSONString;
    }

    static String getDetailedWeather(int queryLat, int queryLon) {
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        String detailedWeatherJSONString = null;

        try{
            Uri builtURI = Uri.parse(DETAILED_OPEN_WEATHER_URL).buildUpon()
                    .appendQueryParameter(LAT, String.valueOf(queryLat))
                    .appendQueryParameter(LON, String.valueOf(queryLon))
                    .appendQueryParameter(EXCLUDE, "current,minutely,hourly,alerts")
                    .appendQueryParameter(UNITS, "imperial")
                    .appendQueryParameter(LANG, "en")
                    .appendQueryParameter(APP_ID, API_KEY)
                    .build();

            URL requestURL = new URL(builtURI.toString());

            urlConnection = (HttpsURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder builder = new StringBuilder();

            String newLine;

            while((newLine = reader.readLine()) != null) {
                builder.append(newLine);
                builder.append("\n");
            }

            if(builder.length() == 0) {
                return null;
            }

            detailedWeatherJSONString = builder.toString();

            detailedWeatherJSONString = detailedWeatherJSONString.replaceAll(",", "\n");

            Log.d(LOG_TAG, detailedWeatherJSONString);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
            if(reader != null) {
                try{
                    reader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return detailedWeatherJSONString;
    }
}
