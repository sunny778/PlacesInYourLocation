package com.example.sunny.androidproject2;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;

// last last
public class MainActivity extends AppCompatActivity implements DialogInterface.OnClickListener, LocationListener, View.OnClickListener {

    private BatteryCharge batteryCharge;
    private PlacesListFragment placesListFragment;
    private InfoFragment infoFragment;
    private FavoritesFragment favoritesFragment;
    private FragmentManager manager;
    private LocationManager locationManager;
    private boolean isTablet;
    protected LatLng userLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = getSupportFragmentManager();

        // create fragments object
        placesListFragment = new PlacesListFragment();
        infoFragment = new InfoFragment();
        favoritesFragment = new FavoritesFragment();

        isTablet = getResources().getBoolean(R.bool.isTablet);

            // first time the application running
            if (findViewById(R.id.fragContainer2) != null
                    ) {
                // Tablet in landscape mode
                manager.beginTransaction()
                        .add(R.id.fragContainer2, infoFragment)
                        .add(R.id.fragContainer, placesListFragment)
                        .add(R.id.fragContainer, favoritesFragment)
                        .hide(favoritesFragment)
                        .commit();
                isTablet = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            } else if (isTablet){
                // Tablet in portrait mode switch him to landscape mode
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                isTablet = true;
            }else {
                // Phone
                manager.beginTransaction()
                        .add(R.id.fragContainer, infoFragment)
                        .add(R.id.fragContainer, placesListFragment)
                        .add(R.id.fragContainer, favoritesFragment)
                        .hide(favoritesFragment)
                        .hide(infoFragment)
                        .commit();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        // check if user have internet connection
        isThereConnection();

        // create a batteryCharge object and start the connection with him
        batteryCharge = new BatteryCharge();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryCharge, filter);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // check if the permission as already accepted
        getUserPermission();

        // set the floatingButton on click listener
        findViewById(R.id.floatingButton).setOnClickListener(this);
    }

    // connect between the menu to MainActivity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // in onDestroy stop listening to batteryCharge
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryCharge);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // check which item clicked in menu
        switch (item.getItemId()) {

            // if menuSettings clicked open the settings fragment
            case R.id.menuSettings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                break;

            // if the menuClearFavorites clicked open dialog to ask the user if he shore about that
            case R.id.menuClearFavorites:
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("DELETE ALL FAVORITES")
                        .setMessage("Are you sure you want to delete all the favorites?")
                        .setPositiveButton("YES", this)
                        .setNegativeButton("NO", this)
                        .setNeutralButton("CANCEL", this)
                        .setIcon(R.drawable.delete)
                        .create();
                dialog.show();

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        // check which button clicked in the dialog
        switch (which) {

            // is he sad yes so delete all the favorites from the database
            case DialogInterface.BUTTON_POSITIVE:
                favoritesFragment.deleteFavorites();
                break;

            case DialogInterface.BUTTON_NEGATIVE:

                break;

            case DialogInterface.BUTTON_NEUTRAL:

                break;
        }
    }

    public void changeToInfoFragment() {

        if (isTablet == false) {
            manager.beginTransaction()
                    .add(R.id.fragContainer, new InfoFragment())
                    .addToBackStack("")
                    .commit();
        }else {
            manager.beginTransaction()
                    .add(R.id.fragContainer2, new InfoFragment())
                    .addToBackStack("")
                    .commit();
        }

    }

    // check if the user already accept the permissions if not ask him
    public void getUserPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE}, 1);
            }
            return;
        }

        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, getMainLooper());
    }

    // update the userLatLng object if the user location changed
    @Override
    public void onLocationChanged(Location location) {
        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
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

    // getter for the user latLng check if is null getLastKnownLocation
    public LatLng getUserLatLng() {
        if (userLatLng == null && locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null) {
                userLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            }
        }
        return userLatLng;
    }

    // on floating button click open the favorites fragment
    @Override
    public void onClick(View v) {

        manager.beginTransaction()
                .show(favoritesFragment)
                .hide(placesListFragment)
                .addToBackStack("")
                .commit();
    }

    //  this method checks if there is internet connection .
    public boolean isThereConnection() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
