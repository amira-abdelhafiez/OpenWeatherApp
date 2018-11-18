package com.example.amira.openweatherapp.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.amira.openweatherapp.R;
import com.example.amira.openweatherapp.models.City;
import com.example.amira.openweatherapp.models.SelectedUserLocation;
import com.example.amira.openweatherapp.utils.JsonUtils;
import com.example.amira.openweatherapp.utils.NetworkUtils;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final int GET_DATA_LOADER_ID = 823;

    private static final String BUNDLE_ID = "bundle";
    private static final String CURRENT_PLACE_ID = "placeId";
    private static final String POSITION_ID = "position";
    private static final String TITLE_ID = "title";
    private int mCurrentId;

    private SelectedUserLocation mLocation;

    // Controls
    @BindView(R.id.tv_temp_value)
    TextView mTemperatureTextView;

    @BindView(R.id.tv_pressure_value)
    TextView mPressureTextView;

    @BindView(R.id.tv_humidity_value)
    TextView mHumidityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mLocation = new SelectedUserLocation();
        Intent callingIntent = getIntent();
        if(callingIntent.hasExtra(BUNDLE_ID)){
            Bundle bundle = callingIntent.getBundleExtra(BUNDLE_ID);
            if(bundle != null){
                mCurrentId = bundle.getInt(CURRENT_PLACE_ID);
                LatLng latLng = bundle.getParcelable(POSITION_ID);
                mLocation.setPosition(latLng);
                mLocation.setName(bundle.getString(TITLE_ID));
            }
        }
        ButterKnife.bind(this);

        getSupportLoaderManager().initLoader(GET_DATA_LOADER_ID , null ,this);
        getData();
    }


    private void getData(){
        Loader loader = getSupportLoaderManager().getLoader(GET_DATA_LOADER_ID);
        if(loader == null){
            getSupportLoaderManager().initLoader(GET_DATA_LOADER_ID , null , this);
        }else{
            getSupportLoaderManager().restartLoader(GET_DATA_LOADER_ID , null , this);
        }
    }

    private void populateData(City city){
        if(city == null) return;
        String tempValue = Double.toString(city.getDayWeathers()[0].getTemperature().getDay());
        String pressureValue = Double.toString(city.getDayWeathers()[0].getPressure());
        String humidityValue = Double.toString(city.getDayWeathers()[0].getHumidity());
        mTemperatureTextView.setText(tempValue);
        mPressureTextView.setText(pressureValue);
        mHumidityTextView.setText(humidityValue);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            String responseStr = "";

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(responseStr != null && !responseStr.isEmpty()){
                    deliverResult(responseStr);
                }else{
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {
                if(mLocation != null){
                    String latStr = Double.toString(mLocation.getPosition().latitude);
                    String lngStr = Double.toString(mLocation.getPosition().longitude);
                    String cnt = "1";
                    URL url = NetworkUtils.getDataQueryUrl(latStr , lngStr , cnt);
                    if(url == null) {
                        Log.d(LOG_TAG , "Null Url");
                        return null;
                    }
                    Log.d(LOG_TAG , url.toString());

                    try {
                        responseStr = NetworkUtils.getQueryResult(url);
                    }catch(IOException e){
                        Log.d(LOG_TAG , e.getMessage());
                    }
                }
                return responseStr;
            }

            @Override
            public void deliverResult(String data) {
                responseStr = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if(data == null){
            Log.d(LOG_TAG , "Null Value");
        }else{
            Log.d("ApiData" , "Data Arrived");
            City city = JsonUtils.parseJsonData(data);
            populateData(city);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

}
