package com.example.sunny.androidproject2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Sunny on 17/05/2017.
 */

public class PlaceProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.sunny.androidproject2.authority.places";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PlaceDBHelper.TABLE_NAME);
    public static final Uri CONTENT_FAVORITES_URI = Uri.parse("content://" + AUTHORITY + "/" + PlaceDBHelper.TABLE_NAME2);

    private PlaceDBHelper helper;

    public PlaceProvider() {
    }

    @Override
    public boolean onCreate() {

        helper = new PlaceDBHelper(getContext());

        if (helper != null){
            return true;
        }
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor;

        // check which url received to know witch table to working with
        if (uri.equals(CONTENT_URI)) {
            cursor = db.query(PlaceDBHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        }else{
            cursor = db.query(PlaceDBHelper.TABLE_NAME2, projection, selection, selectionArgs, null, null, sortOrder);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = helper.getWritableDatabase();
        long rowId;

        // check which url received to know witch table to working with
        if (uri.equals(CONTENT_URI)){
            rowId = db.insert(PlaceDBHelper.TABLE_NAME, null, values);
        }else{
            rowId = db.insert(PlaceDBHelper.TABLE_NAME2, null, values);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return Uri.withAppendedPath(uri, rowId + "");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = helper.getWritableDatabase();
        int count;

        // check which url received to know witch table to working with
        if (uri.equals(CONTENT_URI)){
            count = db.delete(PlaceDBHelper.TABLE_NAME, selection, selectionArgs);
        }else{
            count = db.delete(PlaceDBHelper.TABLE_NAME2, selection, selectionArgs);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = helper.getWritableDatabase();
        int count;

        // check which url received to know witch table to working with
        if (uri.equals(CONTENT_URI)){
            count = db.update(PlaceDBHelper.TABLE_NAME, values, selection, selectionArgs);
        }else{
            count = db.update(PlaceDBHelper.TABLE_NAME2, values, selection, selectionArgs);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
