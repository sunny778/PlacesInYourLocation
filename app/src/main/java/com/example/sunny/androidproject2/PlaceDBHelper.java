package com.example.sunny.androidproject2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sunny on 17/05/2017.
 */

public class PlaceDBHelper extends SQLiteOpenHelper {

    // create finals variables to columns and table names
    public static final String TABLE_NAME = "places";
    public static final String TABLE_NAME2 = "favorites";
    public static final String COL_ID = "id";
    public static final String COL_PLACE_ID = "place_id";
    public static final String COL_NAME = "name";
    public static final String COL_ADDRESS = "address";
    public static final String COL_IMAGE = "image";
    public static final String COL_DISTANCE = "distance";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";

    public PlaceDBHelper(Context context) {
        super(context, "places.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create the places table
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT" +
                ", %s TEXT, %s REAL, %s REAL, %s REAL, %s TEXT)", TABLE_NAME, COL_ID, COL_NAME, COL_ADDRESS, COL_IMAGE,
                COL_DISTANCE, COL_LATITUDE, COL_LONGITUDE, COL_PLACE_ID);

        // create the favorites table
        String sql2 = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT" +
                        ", %s TEXT, %s REAL, %s REAL, %s REAL, %s TEXT)", TABLE_NAME2, COL_ID, COL_NAME, COL_ADDRESS, COL_IMAGE,
                COL_DISTANCE, COL_LATITUDE, COL_LONGITUDE, COL_PLACE_ID);

        db.execSQL(sql);
        db.execSQL(sql2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
