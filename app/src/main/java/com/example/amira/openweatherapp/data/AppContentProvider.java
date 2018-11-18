package com.example.amira.openweatherapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class AppContentProvider extends ContentProvider {

    private static final String LOG_TAG = AppContentProvider.class.getSimpleName();

    private DbHelper mDbHelper;

    private static final int PLACE = 100;
    private static final int PLACE_WITH_ID = 101;


    private static UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DbContract.AUTHORITY , DbContract.PLACE_PATH , PLACE);
        uriMatcher.addURI(DbContract.AUTHORITY , DbContract.PLACE_PATH + "/#" , PLACE_WITH_ID);
        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDbHelper = new DbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        int resultId = sUriMatcher.match(uri);
        Cursor cursor = null;
        switch (resultId){
            case PLACE:
                cursor = db.query(DbContract.OpenWeatherDbEntry.TABLE_NAME,
                        projection ,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            case PLACE_WITH_ID:
                String placeId = uri.getPathSegments().get(1);

                cursor = db.query(DbContract.OpenWeatherDbEntry.TABLE_NAME,
                        projection ,
                        "_id=?",
                        new String[]{placeId},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Invalid uri is " +  uri.toString());
        }

        cursor.setNotificationUri(getContext().getContentResolver() , uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int resultId = sUriMatcher.match(uri);
        Uri retUri;
        switch (resultId){
            case PLACE:
                long id = db.insert(DbContract.OpenWeatherDbEntry.TABLE_NAME, null , contentValues);
                if(id > 0){
                    retUri = ContentUris.withAppendedId(DbContract.OpenWeatherDbEntry.CONTENT_URI, id);
                }else{
                    throw new android.database.SQLException("Invalid uri " + uri);
                }

                break;
            default:
                throw new UnsupportedOperationException("Uri " + uri);
        }
        getContext().getContentResolver().notifyChange(uri , null);
        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int resultId = sUriMatcher.match(uri);
        int deletedCount = 0;
        switch (resultId){
            case PLACE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                deletedCount = db.delete(DbContract.OpenWeatherDbEntry.TABLE_NAME, "id=?" , new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Uri " + uri);
        }
        if(deletedCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int resultId = sUriMatcher.match(uri);
        int updatedCount = 0;
        switch (resultId){
            case PLACE_WITH_ID:
                updatedCount = db.update(DbContract.OpenWeatherDbEntry.TABLE_NAME, values , selection , selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Uri " + uri);
        }
        getContext().getContentResolver().notifyChange(uri , null);
        return updatedCount;
    }
}
