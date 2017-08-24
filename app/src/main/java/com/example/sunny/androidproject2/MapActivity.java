package com.example.sunny.androidproject2;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager manager;
    private LatLng userLocation;
    private String name;
    private double lat;
    private double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // set the map layout
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // get the latitude, longitude and name from intent
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        lat = intent.getDoubleExtra("lat", 0);
        lng = intent.getDoubleExtra("lng", 0);

        getUserLocation();

    }

    // check if the user already accept the permissions if not ask him
    public void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            return;
        }
        manager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, getMainLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getUserLocation();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            getFavorites();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        // set the latitude, longitude and name that receive in the map
        LatLng placeLocation = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(placeLocation).title(name));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLocation, 16));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // set the my location button visible
        mMap.setMyLocationEnabled(true);
    }

    private void getFavorites(){

        // get the favorites latitude, longitude, name
        Cursor c = this.getContentResolver().query(PlaceProvider.CONTENT_FAVORITES_URI, new String[]{PlaceDBHelper.COL_NAME, PlaceDBHelper.COL_LATITUDE, PlaceDBHelper.COL_LONGITUDE}, null, null, null);

        while (c.moveToNext()){
            double lat = c.getDouble(c.getColumnIndex(PlaceDBHelper.COL_LATITUDE));
            double lng = c.getDouble(c.getColumnIndex(PlaceDBHelper.COL_LONGITUDE));
            String name = c.getString(c.getColumnIndex(PlaceDBHelper.COL_NAME));
            // create marker with name each favorite
            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(name));
        }
    }

    // update the userLatLng object if the user location changed
    @Override
    public void onLocationChanged(Location location) {
        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}


