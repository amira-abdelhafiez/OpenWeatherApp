package com.example.amira.openweatherapp.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amira.openweatherapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback , LoaderManager.LoaderCallbacks<String>{

    private static final String LOG_TAG = MapActivity.class.getSimpleName();
    private static final int SAVE_DATA_LOADER_ID = 300;
    private static final int GET_WEATHER_LOADER_ID = 301;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private boolean mIsLocationPermissionGranted = false;
    private static final float DEFAULT_ZOOM = 15f;

    private LatLng mCurrentLatLng;
    private int mCurrentCount;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mGoogleMap;

    @BindView(R.id.et_search)
    EditText mSearchEditText;

    @BindView(R.id.iv_gps)
    ImageView mGPSImageView;

    @BindView(R.id.iv_add_location)
    ImageView mAddLocationImageView;

    @BindView(R.id.tv_places_cnt)
    TextView mPlacesCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ButterKnife.bind(this);

        checkLocationPermission();

        initializeControls();

        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(SAVE_DATA_LOADER_ID , null , this);
        loaderManager.initLoader(GET_WEATHER_LOADER_ID , null , this);
    }

    private void initializeControls(){

        mSearchEditText.setSingleLine();
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_SEARCH
                        || id == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    Log.d("Hello" , "Clicked");
                    String searchText = mSearchEditText.getText().toString();

                    Geocoder geocoder = new Geocoder(MapActivity.this);
                    List<Address> addressList = new ArrayList<>();
                    try{
                        addressList = geocoder.getFromLocationName(searchText ,1);
                    }catch (IOException e){
                        Log.d(LOG_TAG , e.getMessage());
                    }

                    if(addressList.size() > 0){
                        Address address = addressList.get(0);
                        Log.d(LOG_TAG , address.toString());
                        LatLng latLng = new LatLng(address.getLatitude() , address.getLongitude());
                        mCurrentLatLng = latLng;
                        pinPointLocation(latLng , DEFAULT_ZOOM , address.getAddressLine(0));
                    }
                }
                return true;
            }
        });

        mGPSImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserCurrentPosition();
            }
        });

        mAddLocationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryWeatherAndSaveToDb();
            }
        });
        mPlacesCount.setText("No places selected");
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void increaseCount(){
        mCurrentCount++;
        mPlacesCount.setText(mCurrentCount + " Locations");
    }

    private void queryWeatherAndSaveToDb(){
        if(mCurrentLatLng == null){
            Toast.makeText(MapActivity.this , "No Location Selected" , Toast.LENGTH_SHORT).show();
        }else{
            Loader loader = getSupportLoaderManager().getLoader(GET_WEATHER_LOADER_ID);
            if(loader != null){
                getSupportLoaderManager().restartLoader(GET_WEATHER_LOADER_ID , null , this);
            }else{
                getSupportLoaderManager().initLoader(GET_WEATHER_LOADER_ID , null , this);
            }
            increaseCount();
        }
    }

    private void getUserCurrentPosition() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mIsLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location userCurrentLocation = (Location) task.getResult();
                            LatLng latLng = new LatLng(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude());
                            mCurrentLatLng = latLng;
                            pinPointLocation(latLng, DEFAULT_ZOOM , "Your Location");
                        } else {
                            Log.d(LOG_TAG, "Couldn't get user current location.");
                            Toast.makeText(MapActivity.this, "Sorry , We couldn't get your current location.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    private void pinPointLocation(LatLng latLng, float zoom , String title) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        mGoogleMap.addMarker(markerOptions);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location Permission Needed", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(MapActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        } else {
            Toast.makeText(this, "Location Permission is already granted", Toast.LENGTH_LONG).show();
            mIsLocationPermissionGranted = true;
            initMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mIsLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mIsLocationPermissionGranted = true;
                    initMap();
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (mIsLocationPermissionGranted) {
            getUserCurrentPosition();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }


    // This Loader Queries the API to get the Weather for the location
    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        if(id == SAVE_DATA_LOADER_ID){
            return new AsyncTaskLoader<String>(this) {
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                }

                @Override
                public String loadInBackground() {
                    return null;
                }

                @Override
                public void forceLoad() {
                    super.forceLoad();
                }
            };
        }else if(id == GET_WEATHER_LOADER_ID) {
            if(mCurrentLatLng == null){
                return null;
            }
            return new AsyncTaskLoader<String>(this) {
                @Override
                public String loadInBackground() {
                    return null;
                }
            };
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        int id = loader.getId();
        if(id == SAVE_DATA_LOADER_ID){

        }else if(id == GET_WEATHER_LOADER_ID){

        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
