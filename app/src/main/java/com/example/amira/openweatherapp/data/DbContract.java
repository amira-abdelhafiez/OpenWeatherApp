package com.example.amira.openweatherapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class DbContract {

    public static final String AUTHORITY = "com.example.amira.openweatherapp";
    public static final String PLACE_PATH = "place";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class OpenWeatherDbEntry implements BaseColumns{
        
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PLACE_PATH).build();

        /// Table Name
        public static final String TABLE_NAME = "places";

        // Table columns
        public static final String ID_COL = "_id";
        public static final String NAME_COL = "name";
        public static final String TEMP_COL = "temperature";
        public static final String PRESSURE_COL = "pressure";
        public static final String HUMIDITY_COL = "humidity";
        public static final String CLOUDS_COL = "clouds";
        public static final String LATITUDE_COL = "latitude";
        public static final String LONGITUDE_COL = "longitude";
    }
}
