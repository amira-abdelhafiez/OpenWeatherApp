package com.example.amira.openweatherapp.utils;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    private static final String API_KEY = "83e5ee4594994b3a86feebde68a7eb54";
    private static final String API_KEY_2 = "2061db6b7baa4603bd70ee761b02f0c3";

    private static final String BASE_URL = "api.openweathermap.org/data/2.5/forecast/daily";
    private static final String BASE_SAMPLES_URL = "https://samples.openweathermap.org/data/2.5/forecast/daily";

    private static final String LAT_PARAM = "lat";
    private static final String LONG_PARAM = "lon";
    private static final String DAYS_COUNT_PARAM = "cnt";
    private static final String KEY_PARAM = "appid";


    // Forms and Returns the query Url from the incoming data
    public static URL getDataQueryUrl(String latValue , String longValue , String daysCount){
        //Using Sample base URL.
        //TODO : If the API key is valid Please Replace BASE_SAMPLES_URL with the BASE_URL
        Uri uri = Uri.parse(BASE_SAMPLES_URL).buildUpon()
                .appendQueryParameter(LAT_PARAM , latValue)
                .appendQueryParameter(LONG_PARAM , longValue)
                .appendQueryParameter(DAYS_COUNT_PARAM , daysCount)
                .appendQueryParameter(KEY_PARAM , API_KEY)
                .build();
        URL url = null;
        try{
            url = new URL(uri.toString());

        }catch(MalformedURLException e){
            Log.e(LOG_TAG , e.getStackTrace().toString());
        }
        return url;
    }


    // Send Request to the OpenWeather Api and receive JSON data
    public static String getQueryResult(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try{
            InputStream in = connection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasNext = scanner.hasNext();
            if(hasNext){
                return scanner.next();
            }else{
                return null;
            }
        } finally {
            connection.disconnect();
        }
    }


}
