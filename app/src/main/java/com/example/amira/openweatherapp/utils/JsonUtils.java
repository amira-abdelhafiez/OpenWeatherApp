package com.example.amira.openweatherapp.utils;

import android.util.Log;

import com.example.amira.openweatherapp.models.City;
import com.example.amira.openweatherapp.models.DayWeather;
import com.example.amira.openweatherapp.models.Temperature;
import com.example.amira.openweatherapp.models.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class JsonUtils {
    // Debug
    private static final String LOG_TAG = JsonUtils.class.getSimpleName();

    // Parsing
    private static final String CITY = "city";
    private static final String LIST = "list";
    private static final String GEO_ID = "geoname_id";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lon";
    private static final String CITY_NAME = "name";
    private static final String DAY_ID = "dt";
    private static final String TEMPERATURE = "temp";
    private static final String TEMPERATURE_MIN  = "min";
    private static final String TEMPERATURE_MAX = "max";
    private static final String TEMPERATURE_DAY = "day";
    private static final String PRESSURE = "pressure";
    private static final String HUMIDITY = "humidity";
    private static final String WEATHER = "weather";
    private static final String WEATHER_ID = "id";
    private static final String WEATHER_MAIN = "main";
    private static final String WEATHER_DESCRIPTION = "description";
    private static final String SPEED = "speed";
    private static final String DEGREE = "deg";
    private static final String CLOUDS = "clouds";

    public static City parseJsonData(String jsonData){
        City city = new City();
        if(jsonData != null){
            try{

                JSONObject obj = new JSONObject(jsonData);
                // Parsing the location/city Data
                JSONObject cityJsonObject = obj.getJSONObject(CITY);
                city.setGeoId(cityJsonObject.optInt(GEO_ID));
                city.setLatitude(cityJsonObject.optDouble(LATITUDE));
                city.setLongitude(cityJsonObject.optDouble(LONGITUDE));
                city.setName(cityJsonObject.optString(CITY_NAME));
                // Parsing Days Weather Data
                JSONArray weatherDaysArray = obj.getJSONArray(LIST);
                int len = weatherDaysArray.length();
                DayWeather[] dayWeathers = new DayWeather[len];
                JSONObject dayWeatherJson , temperatureJsonObject;
                JSONArray weatherArrayList;

                Temperature dayTemperature;
                Weather[] weatherList;
                for(int i = 0 ; i < len ; i++){
                    dayWeathers[i] = new DayWeather();
                    dayWeatherJson = weatherDaysArray.getJSONObject(i);
                    dayWeathers[i].setId(dayWeatherJson.optInt(DAY_ID));
                    dayWeathers[i].setDegree(dayWeatherJson.optDouble(DEGREE));
                    dayWeathers[i].setHumidity(dayWeatherJson.optDouble(HUMIDITY));
                    dayWeathers[i].setClouds(dayWeatherJson.optDouble(CLOUDS));
                    dayWeathers[i].setPressure(dayWeatherJson.optDouble(PRESSURE));
                    dayWeathers[i].setSpeed(dayWeatherJson.optDouble(SPEED));

                    // Get The Temperature Object inside each list item
                    dayTemperature = new Temperature();
                    temperatureJsonObject = dayWeatherJson.getJSONObject(TEMPERATURE);
                    dayTemperature.setDay(temperatureJsonObject.optDouble(TEMPERATURE_DAY));
                    dayTemperature.setMax(temperatureJsonObject.optDouble(TEMPERATURE_MAX));
                    dayTemperature.setMin(temperatureJsonObject.optDouble(TEMPERATURE_MIN));

                    dayWeathers[i].setTemperature(dayTemperature);

                    // Get the Weather List Inside the DayWeather List item
                    weatherArrayList = dayWeatherJson.getJSONArray(WEATHER);
                    int weatherLen = weatherArrayList.length();
                    weatherList = new Weather[weatherLen];
                    JSONObject weatherJsonObj;
                    for(int j = 0 ; j < weatherLen ; j++){
                        weatherJsonObj = weatherArrayList.getJSONObject(j);
                        weatherList[j] = new Weather();
                        weatherList[j].setId(weatherJsonObj.optInt(WEATHER_ID));
                        weatherList[j].setDescription(weatherJsonObj.optString(WEATHER_DESCRIPTION));
                        weatherList[j].setMain(weatherJsonObj.optString(WEATHER_MAIN));
                    }
                    dayWeathers[i].setWeathers(weatherList);
                }

                city.setDayWeathers(dayWeathers);
            }catch (JSONException e){
                Log.d(LOG_TAG , e.getMessage());
            }
        }else{
            Log.d(LOG_TAG , "Null Json Data");
        }
        return city;
    }

}
