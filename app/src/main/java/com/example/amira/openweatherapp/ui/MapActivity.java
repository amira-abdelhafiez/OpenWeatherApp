package com.example.amira.openweatherapp.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amira.openweatherapp.R;
import com.example.amira.openweatherapp.adapters.PlaceAutoCompleteAdapter;
import com.example.amira.openweatherapp.data.DbContract;
import com.example.amira.openweatherapp.models.SelectedUserLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback , LoaderManager.LoaderCallbacks<Uri> , GoogleApiClient.OnConnectionFailedListener{

    private static final String LOG_TAG = MapActivity.class.getSimpleName();
    private static final int SAVE_DATA_LOADER_ID = 300;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private boolean mIsLocationPermissionGranted = false;
    private static final float DEFAULT_ZOOM = 15f;

    private static final String LATITIUDE_KEY = "lat";
    private static final String LONGITUDE_KEY = "lon";
    private static final String NAME_KEY = "name";

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40 , -168) , new LatLng(71 , 136)
    );
    //private LatLng mCurrentLatLng;
    private SelectedUserLocation mCurrentSelectedLocation;
    private int mCurrentCount;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutoCompleteAdapter mAutoCompleteAdapter;
    private GoogleMap mGoogleMap;

    private MarkerOptions mMarkerOptions;
    private Marker mMarker;

    @BindView(R.id.et_search)
    AutoCompleteTextView mSearchEditText;

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
    }

    private void initializeControls(){

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this , this)
                .build();
        mAutoCompleteAdapter = new PlaceAutoCompleteAdapter(this , mGoogleApiClient , LAT_LNG_BOUNDS , null);
        mSearchEditText.setOnItemClickListener(mAutoCompleteClickListener);
        mSearchEditText.setAdapter(mAutoCompleteAdapter);
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
                        //mCurrentLatLng = latLng;
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
                saveData();
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

    private void saveData(){
        if(mCurrentSelectedLocation == null){
            Toast.makeText(MapActivity.this , "No Location Selected" , Toast.LENGTH_SHORT).show();
        }else{
            Loader loader = getSupportLoaderManager().getLoader(SAVE_DATA_LOADER_ID);
            Bundle bundle = new Bundle();
            bundle.putDouble(LATITIUDE_KEY , mCurrentSelectedLocation.getPosition().latitude);
            bundle.putDouble(LONGITUDE_KEY , mCurrentSelectedLocation.getPosition().longitude);
            bundle.putString(NAME_KEY , mCurrentSelectedLocation.getName());
            if(loader != null){

                getSupportLoaderManager().restartLoader(SAVE_DATA_LOADER_ID , bundle , this);
            }else{
                getSupportLoaderManager().initLoader(SAVE_DATA_LOADER_ID , bundle , this);
            }
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
                            //mCurrentLatLng = latLng;
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

        if(mMarker == null){
            mMarkerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMarker = mGoogleMap.addMarker(mMarkerOptions);
        }else{
            mMarker.setPosition(latLng);
            mMarker.setTitle(title);
        }

        if(mCurrentSelectedLocation == null){
            mCurrentSelectedLocation = new SelectedUserLocation();
        }
        mCurrentSelectedLocation.setName(title);
        mCurrentSelectedLocation.setPosition(latLng);
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
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                String title = "Selected Location : " + latLng.toString();
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses.size() > 0) {
                        title = addresses.get(0).getAddressLine(0);
                    }
                }catch(IOException e){
                    Log.d(LOG_TAG , e.getMessage());
                }
                pinPointLocation(latLng , DEFAULT_ZOOM , title + latLng.toString());
            }
        });
        if (mIsLocationPermissionGranted) {
            getUserCurrentPosition();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

        }
    }

    @Override
    public Loader<Uri> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Uri>(this) {
            Uri retUri;
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(args == null){
                    Log.d(LOG_TAG , "null args");
                    return;
                }

                if(retUri != null){
                    deliverResult(retUri);
                }else{
                    forceLoad();
                }
            }

            @Override
            public Uri loadInBackground() {
                double lat = args.getDouble(LATITIUDE_KEY);
                double lon = args.getDouble(LONGITUDE_KEY);
                String name = args.getString(NAME_KEY);

                Uri uri = null;
                try{
                    ContentValues values = new ContentValues();
                    values.put(DbContract.OpenWeatherDbEntry.LATITUDE_COL , lat);
                    values.put(DbContract.OpenWeatherDbEntry.LONGITUDE_COL , lon);
                    values.put(DbContract.OpenWeatherDbEntry.NAME_COL , name);
                    uri = getContentResolver().insert(DbContract.OpenWeatherDbEntry.CONTENT_URI, values);
                }catch(Exception e){
                    Log.d(LOG_TAG , "Problem Inserting Location");
                }
                return uri;
            }

            @Override
            public void deliverResult(Uri data) {
                retUri = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Uri> loader, Uri data) {
        String Message;
        if(data == null){
            Log.d(LOG_TAG , "Place not Saved");
            Message = "Location Not Saved";
        }else{
            Log.d(LOG_TAG , "Place Saved");
            Message = "Location Saved Successfully";
            increaseCount();
        }
        Toast.makeText(MapActivity.this , Message , Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLoaderReset(Loader<Uri> loader) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private AdapterView.OnItemClickListener mAutoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final AutocompletePrediction prediction = mAutoCompleteAdapter.getItem(i);
            final String placeId = prediction.getPlaceId();
            PendingResult<PlaceBuffer> pendingResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient , placeId);
            pendingResult.setResultCallback(mPlaceDataQueryCallback);

        }
    };

    private ResultCallback<PlaceBuffer> mPlaceDataQueryCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(LOG_TAG , "Failed to get the place info");
                places.release();
                return;
            }


            final Place place = places.get(0);
            try{
                LatLng latLng = new LatLng(place.getViewport().getCenter().latitude ,
                        place.getViewport().getCenter().longitude);

                pinPointLocation(latLng , DEFAULT_ZOOM , place.getName().toString());
            }catch(Exception e){
                Log.d(LOG_TAG , "Error: " +  e.getMessage());
            }

            places.release();
        }
    };
}
