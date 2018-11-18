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

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> , PlacesAdapter.OnItemClickHandler{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
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

    }

    private void hideLoadingBar(){

    }

    private void showEmptyMessage(){

    }

    private void hideEmptyMessage(){

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == GET_DATA_LOADER){
            return new AsyncTaskLoader<Cursor>(this) {
                Cursor cursor;
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
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
        if(id == GET_DATA_LOADER){
            mDataCursor = data;
            mAdapter.setmCursor(data);
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
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(GET_DATA_LOADER, null  , this);
    }
}
