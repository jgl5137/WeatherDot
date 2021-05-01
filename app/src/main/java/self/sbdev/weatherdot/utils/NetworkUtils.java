package self.sbdev.weatherdot.utils;

import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class NetworkUtils {
//    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    private static final String API_KEY = "e83f7c0a2b6d6c8316fea85fb334134c";
    //Base URL for OpenWeather API
    private static final String LAT_LON_OPEN_WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?";
    //Parameter for the search string.
    private static final String QUERY_PARAM = "q";
    //Parameter that corresponds to temperature measurement type (Fahrenheit or Celsius).
    private static final String UNITS = "units";
    //Parameter for the API Key
    private static final String APP_ID = "appid";
    //Parameter that corresponds to the user's preferred language.
    private static final String LANG = "lang";
    //Base URL for OpenWeather One Call API
    private static final String DETAILED_OPEN_WEATHER_URL = "https://api.openweathermap.org/data/2.5/onecall?";
    //Required parameter for the One Call API (Latitude).
    private static final String LAT = "lat";
    //Required parameter for the One Call API (Longitude).
    private static final String LON = "lon";
    //Parameter that excludes certain types of data (ex. minutely, hourly, etc...).
    private static final String EXCLUDE = "exclude";

    /**
     * Method that initializes the call to the OpenWeather API and retrieves the most recent Weather information for the city.
     * @param queryString The name of the city that the user is searching for.
     * @param measurementType The user's preferred measurement type.
     * @param language The user's preferred language they want the data in.
     * @return A String containing all of the retrieved data.
     */
    public static String getLatLon(String queryString, String measurementType, String language) {
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        String latLonJSONString = null;
        String measureType = "";
        String chosenLanguage = "";

        //Dependant on user's 'Measurement' setting.
        if(measurementType.equalsIgnoreCase("celsius")) {
            measureType = "metric";
        }
        else {
            measureType = "imperial";
        }

        //Dependant on user's 'Language' setting.
        switch (language) {
            case "en":
                chosenLanguage = "en";
                break;
            case "es":
                chosenLanguage = "es";
                break;
            case "fr":
                chosenLanguage = "fr";
                break;
            case "de":
                chosenLanguage = "de";
                break;
            case "zh_cn":
                chosenLanguage = "zh_cn";
                break;
            case "ja":
                chosenLanguage = "ja";
                break;
            case "kr":
                chosenLanguage = "kr";
        }

        try{
            //Building the URL
            Uri builtURI = Uri.parse(LAT_LON_OPEN_WEATHER_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, queryString)
                    .appendQueryParameter(UNITS, measureType)
                    .appendQueryParameter(LANG, chosenLanguage)
                    .appendQueryParameter(APP_ID, API_KEY)
                    .build();

            URL requestURL = new URL(builtURI.toString());

            urlConnection = (HttpsURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Get the InputStream
            InputStream inputStream = urlConnection.getInputStream();

            //Create a buffered reader from that input stream
            reader = new BufferedReader(new InputStreamReader(inputStream));

            //Use StringBuilder to hold the incoming response
            StringBuilder builder = new StringBuilder();

            String newLine;
            while ((newLine = reader.readLine()) != null) {
                //Inputting the JSON into the StringBuilder.
                builder.append(newLine);
            }

            if(builder.length() == 0) {
                //Stream was empty. No point in parsing.
                return null;
            }

            latLonJSONString = builder.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            //Shutting down the HTTPS connection and closing the BufferedReader
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
        //Sending the data back to the Main Activity.
        return latLonJSONString;
    }

    /**
     * Method that initializes the One Call API and retrieves the daily Weather information for the city.
     * @param queryLat The city's latitude
     * @param queryLon The city's longitude
     * @param measurementType The user's preferred measurement type.
     * @param language The user's preferred language they want the data in.
     * @return A String containing all of the retrieved data.
     */
    public static String getWeather(double queryLat, double queryLon, String measurementType, String language) {
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        String weatherJSONString = null;
        String measureType = "";
        String chosenLanguage = "";

        //Dependant on user's 'Measurement' setting.
        if(measurementType.equalsIgnoreCase("celsius")) {
            measureType = "metric";
        }
        else {
            measureType = "imperial";
        }

        //Dependant on user's 'Language' setting.
        switch (language) {
            case "en":
                chosenLanguage = "en";
                break;
            case "es":
                chosenLanguage = "es";
                break;
            case "fr":
                chosenLanguage = "fr";
                break;
            case "de":
                chosenLanguage = "de";
                break;
            case "zh_cn":
                chosenLanguage = "zh_cn";
                break;
            case "ja":
                chosenLanguage = "ja";
                break;
            case "kr":
                chosenLanguage = "kr";
        }

        try{
            //Building the URL
            Uri builtURI = Uri.parse(DETAILED_OPEN_WEATHER_URL).buildUpon()
                    .appendQueryParameter(LAT, String.valueOf(queryLat))
                    .appendQueryParameter(LON, String.valueOf(queryLon))
                    .appendQueryParameter(EXCLUDE, "minutely,hourly,alerts")
                    .appendQueryParameter(UNITS, measureType)
                    .appendQueryParameter(LANG, chosenLanguage)
                    .appendQueryParameter(APP_ID, API_KEY)
                    .build();

            URL requestURL = new URL(builtURI.toString());

            urlConnection = (HttpsURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Get the InputStream
            InputStream inputStream = urlConnection.getInputStream();

            //Create a buffered reader from that input stream
            reader = new BufferedReader(new InputStreamReader(inputStream));

            //Use a StringBuilder to hold the incoming response
            StringBuilder builder = new StringBuilder();

            String newLine;
            while((newLine = reader.readLine()) != null) {
                builder.append(newLine);
            }

            if(builder.length() == 0) {
                //Stream was empty. No point in parsing.
                return null;
            }

            weatherJSONString = builder.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            //Shutting down the HTTPS connection and closing the BufferedReader
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
        //Sending the data back to the Main Activity.
        return weatherJSONString;
    }
}
