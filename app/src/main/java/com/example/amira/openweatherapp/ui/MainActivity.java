package com.example.amira.openweatherapp.ui;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amira.openweatherapp.R;
import com.example.amira.openweatherapp.adapters.PlacesAdapter;
import com.example.amira.openweatherapp.data.DbContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> , PlacesAdapter.OnItemClickHandler{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String BUNDLE_ID = "bundle";
    private static final String CURRENT_PLACE_ID = "placeId";
    private static final String POSITION_ID = "position";
    private static final String TITLE_ID = "title";
    private static final int GET_DATA_LOADER = 543;
    private Cursor mDataCursor;

    private PlacesAdapter mAdapter;

    @BindView(R.id.rv_places_list)
    RecyclerView mPLacesRecyclerView;

    @BindView(R.id.tv_empty_list_text)
    TextView mEmptyTextView;

    @BindView(R.id.pb_loading_places)
    ProgressBar mLoadingBar;

    @BindView(R.id.fab)
    FloatingActionButton mAddButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getDataFromCP();

        mAdapter = new PlacesAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this , LinearLayoutManager.VERTICAL , false);

        mPLacesRecyclerView.setAdapter(mAdapter);
        mPLacesRecyclerView.setLayoutManager(linearLayoutManager);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkGooglePlayServices()) {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(intent);
                }
            }
        });
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(GET_DATA_LOADER, null , this);
    }

    private boolean checkGooglePlayServices(){
        int Available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if(Available == ConnectionResult.SUCCESS){
            Log.d(LOG_TAG , "Google Play Service is OK");
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(Available)){
            Log.d(LOG_TAG , "Google Play Service available but there are some problems");
            Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this , Available , 9001);
            errorDialog.show();
        }else{
            Log.d(LOG_TAG , "Google Play Services is not available om this device");
            Toast.makeText(MainActivity.this , "Sorry , We can't display your map" , Toast.LENGTH_LONG).show();
        }
        return false;
    }
    private void getDataFromCP(){
        LoaderManager lm = getSupportLoaderManager();
        Loader loader = lm.getLoader(GET_DATA_LOADER);
        if(loader == null){
            lm.initLoader(GET_DATA_LOADER, null , this);
        }else{
            lm.restartLoader(GET_DATA_LOADER, null , this);
        }
    }

    private void showLoadingBar(){
        mLoadingBar.setVisibility(View.VISIBLE);
        mPLacesRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideLoadingBar(){
        mLoadingBar.setVisibility(View.INVISIBLE);
        mPLacesRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyMessage(){
        mPLacesRecyclerView.setVisibility(View.INVISIBLE);
        mEmptyTextView.setVisibility(View.VISIBLE);
    }

    private void hideEmptyMessage(){
        mPLacesRecyclerView.setVisibility(View.VISIBLE);
        mEmptyTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == GET_DATA_LOADER){
            return new AsyncTaskLoader<Cursor>(this) {
                Cursor cursor;
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    hideEmptyMessage();
                    showLoadingBar();
                    if(cursor != null){
                        deliverResult(cursor);
                    }else{
                        forceLoad();
                    }
                }

                @Override
                public Cursor loadInBackground() {
                    try {
                        return getContentResolver().query(DbContract.OpenWeatherDbEntry.CONTENT_URI,
                                null, null, null, null);
                    }catch(Exception e){
                        Log.d(LOG_TAG , "Can't get Data Async");
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public void deliverResult(Cursor data) {
                    cursor = data;
                    super.deliverResult(data);
                }
            };
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        hideLoadingBar();
        if(id == GET_DATA_LOADER){
            if(data == null || data.getCount() < 1){
                showEmptyMessage();
            }else{
                mDataCursor = data;
                mAdapter.setmCursor(data);
            }

        }else{
            Log.d(LOG_TAG , "Invalid Loader Id " + id);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setmCursor(null);
    }

    @Override
    public void onClick(int position) {
        Intent intent = new Intent(MainActivity.this , DetailActivity.class);
        if(mDataCursor.moveToPosition(position)) {
            LatLng latLng = new LatLng(mDataCursor.getDouble(mDataCursor.getColumnIndex(DbContract.OpenWeatherDbEntry.LATITUDE_COL)), mDataCursor.getDouble(mDataCursor.getColumnIndex(DbContract.OpenWeatherDbEntry.LONGITUDE_COL)));

            Bundle bundle = new Bundle();
            bundle.putInt(CURRENT_PLACE_ID, mDataCursor.getInt(mDataCursor.getColumnIndex(DbContract.OpenWeatherDbEntry.ID_COL)));
            bundle.putParcelable(POSITION_ID, latLng);
            bundle.putString(TITLE_ID, mDataCursor.getString(mDataCursor.getColumnIndex(DbContract.OpenWeatherDbEntry.NAME_COL)));
            intent.putExtra(BUNDLE_ID , bundle);
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG , "Called");
        getSupportLoaderManager().restartLoader(GET_DATA_LOADER, null  , this);
    }
}
