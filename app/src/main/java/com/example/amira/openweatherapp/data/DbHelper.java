package com.example.amira.openweatherapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "openWeatherDb.db";
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context){
        super(context , DATABASE_NAME , null , DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_TABLE_PLACES = "CREATE TABLE " + DbContract.OpenWeatherDbEntry.TABLE_NAME
                + " ( " + DbContract.OpenWeatherDbEntry.ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + DbContract.OpenWeatherDbEntry.NAME_COL + " TEXT NOT NULL ,"
                + DbContract.OpenWeatherDbEntry.TEMP_COL + " REAL NOT NULL ,"
                + DbContract.OpenWeatherDbEntry.LATITUDE_COL + " REAL NOT NULL , "
                + DbContract.OpenWeatherDbEntry.LONGITUDE_COL + " REAL NOT NULL )";

        sqLiteDatabase.execSQL(CREATE_TABLE_PLACES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        final String ALERT_TABLE_PLACES = "ALTER TABLE "
                + DbContract.OpenWeatherDbEntry.TABLE_NAME + " ADD COLUMN new_column string;";

        if(oldVersion < 2){
            sqLiteDatabase.execSQL(ALERT_TABLE_PLACES);
        }
    }
}
