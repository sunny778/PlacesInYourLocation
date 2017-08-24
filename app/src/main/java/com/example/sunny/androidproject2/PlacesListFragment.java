package com.example.sunny.androidproject2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import static android.os.Looper.getMainLooper;

/**
 * A simple {@link Fragment} subclass.
 */

public class PlacesListFragment extends Fragment implements SearchView.OnQueryTextListener, View.OnClickListener {

    public static final String DEFAULT_RADIUS = "1500";
    public static final String MAX_RADIUS = "50000";
    public static final String MIN_RADIUS = "500";

    private SharedPreferences sp;
    private SearchView searchView;
    private PlaceAdapter adapter;
    private ArrayList<Place> places;
    private RecyclerView placesList;
    private String searchName;
    private String radius;
    private LocationManager locationManager;
    private ProgressDialog progressDialog;
    private MainActivity mainActivity;

    public PlacesListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_places_list, container, false);

        // TODO check if need that
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        // get the views from xml
        searchView = (SearchView) view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);
        placesList = (RecyclerView) view.findViewById(R.id.placesList);

        // set the adapter and array list
        adapter = new PlaceAdapter(getContext(), null);
        places = new ArrayList<>();

        // connect between the adapter and xml list
        placesList.setLayoutManager(new LinearLayoutManager(getContext()));
        placesList.setAdapter(adapter);
        // create MainActivity object
        mainActivity = (MainActivity) getContext();

        // create the SharedPreferences
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // try to get the last search from database if there is
        try {
            getLastSearch();

        }catch (Exception e){
            Log.i("DB", "There is a problem with getting the last search info");
        }

        // connect to JsonService with Broadcast/Receiver
        JsonReceiver jsonReceiver = new JsonReceiver();
        IntentFilter filter = new IntentFilter(JsonService.ACTION_JSON);
        LocalBroadcastManager.getInstance(mainActivity).registerReceiver(jsonReceiver, filter);

        // connect to DistanceService with Broadcast/Receiver
        DistanceReceiver distanceReceiver = new DistanceReceiver();
        IntentFilter filter2 = new IntentFilter(DistanceService.ACTION_DISTANCE);
        LocalBroadcastManager.getInstance(mainActivity).registerReceiver(distanceReceiver, filter2);

        // set the on click button listener
        view.findViewById(R.id.buttonNoFilter).setOnClickListener(this);

        return view;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        // check if user have internet connection
        boolean isHaveConnection = ((MainActivity)getActivity()).isThereConnection();

        if (isHaveConnection) {
            // clear the old search
            places.clear();
            adapter.setPlaces(places);

            // get the radius from SettingsFragment with SharedPreferences
            radius = sp.getString("edit_text_preference", DEFAULT_RADIUS);

            // check if the radius that user choose is out of the frame
            if ((Integer.parseInt(radius)) > (Integer.parseInt(MAX_RADIUS))){
                radius = MAX_RADIUS;
            }else if ((Integer.parseInt(radius)) < (Integer.parseInt(MIN_RADIUS))){
                radius = MIN_RADIUS;
            }
            // if the user use space replace it to %20 for the url
            searchName = query.replace(" ", "%20");

            // get the user latLng
            LatLng latLng = mainActivity.getUserLatLng();

            Intent intent = new Intent(mainActivity, JsonService.class);
            // send with intent the url+user latLng, user search text and radius to JsonService
            try {
                intent.putExtra("url", String.format("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s,%s&radius=%s&name=%s" +
                        "&key=AIzaSyCuPa7jSImjuIPE-CSDrVE4DZWa9yb0C2Q", latLng.latitude, latLng.longitude, radius, searchName));
                mainActivity.startService(intent);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            // if there is some old search in database delete it
            try {
                mainActivity.getContentResolver().delete(PlaceProvider.CONTENT_URI, null, null);
            } catch (Exception e) {
                Log.d("Search Place", "There is nothing in database");
            }
        }

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        return false;
    }

    @Override
    public void onClick(View v) {

        // check if user have internet connection
        boolean isHaveConnection = ((MainActivity)getActivity()).isThereConnection();

        if (isHaveConnection) {
            // clear the old search
            places.clear();
            adapter.setPlaces(places);


            // get the radius from SettingsFragment with SharedPreferences
            radius = sp.getString("edit_text_preference", DEFAULT_RADIUS);

            // check if the radius that user choose is out of the frame
            if ((Integer.parseInt(radius)) > (Integer.parseInt(MAX_RADIUS))){
                radius = MAX_RADIUS;
            }else if ((Integer.parseInt(radius)) < (Integer.parseInt(MIN_RADIUS))){
                radius = MIN_RADIUS;
            }
            // get the user latLng
            LatLng latLng = mainActivity.getUserLatLng();

            Intent intent = new Intent(mainActivity, JsonService.class);
            // send with intent the url+user latLng and radius to JsonService
            try {
                intent.putExtra("url", String.format("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s,%s&radius=%s" +
                        "&key=AIzaSyCuPa7jSImjuIPE-CSDrVE4DZWa9yb0C2Q", latLng.latitude, latLng.longitude, radius));
                mainActivity.startService(intent);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            // if there is some old search in database delete it
            try {
                mainActivity.getContentResolver().delete(PlaceProvider.CONTENT_URI, null, null);
            } catch (Exception e) {
                Log.d("Search Place", "There is nothing in database");
            }
        }
    }

    // receive the place info json from JsonService
    private class JsonReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            String json = intent.getStringExtra("json");

            try {

                JSONObject jsonObj = new JSONObject(json);
                JSONArray results = jsonObj.getJSONArray("results");

                if (results.isNull(0)) {
                    try {
                        dismissProgress();
                        Toast.makeText(mainActivity, "Sorry there is no result for this search", Toast.LENGTH_SHORT).show();
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                } else {

                    try {
                        loadingProgress(100, "Looking for results", results.length());
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    for (int i = 0; i < results.length(); i++) {

                        // get the all info with the json object
                        JSONObject placeObj = results.getJSONObject(i);
                        String name = placeObj.getString("name");
                        String placeId = placeObj.getString("place_id");
                        String address = placeObj.getString("vicinity");
                        JSONObject geometry = placeObj.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        double lat = location.getDouble("lat");
                        double lng = location.getDouble("lng");
                        JSONArray photosArr = placeObj.getJSONArray("photos");
                        JSONObject photosObj = photosArr.getJSONObject(0);
                        // set a default image
                        String image = "http://cliparts.co/cliparts/kTM/dBd/kTMdBd9Tj.png";
                        // try to get image from the json
                        try {
                            image = photosObj.getString("photo_reference");
                        }catch (Exception e){
                            Log.d("image", "Json image exception");
                        }

                        // send the info to getPlaceDistance method
                        getPlaceDistance(name, address, image, lat, lng, placeId);

                    }

                }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    public void getPlaceDistance(String name, String address, String image, double lat, double lng, String placeId){

        // get the user latLng
        LatLng latLng = mainActivity.getUserLatLng();

        Intent intent = new Intent(mainActivity, DistanceService.class);

        // send with intent the place info and url+user latLng and place latLng to DistanceService
        intent.putExtra("name", name);
        intent.putExtra("address", address);
        intent.putExtra("image", image);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        intent.putExtra("placeId", placeId);

        intent.putExtra("url", String.format("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=%s,%s&destinations=%s,%s",
        latLng.latitude, latLng.longitude, lat, lng));

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mainActivity.startService(intent);
    }

    private Place place;

    // receive the place info + distance from DistanceService
    private class DistanceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String name = intent.getStringExtra("name");
            String address = intent.getStringExtra("address");
            String image = intent.getStringExtra("image");
            String placeId = intent.getStringExtra("placeId");
            String distance = intent.getStringExtra("distance");
            double lat = intent.getDoubleExtra("lat", 0);
            double lng = intent.getDoubleExtra("lng", 0);

            // create a place object with the all info
            place = new Place(name, address, image, lat, lng, placeId, distance);

            // add the place object to places(ArrayList)
            places.add(place);

            // insert the place into database
            insertPlaceIntoDB(place);

            // set the adapter with the new update places(ArrayList)
            adapter.setPlaces(places);
            placesList.setAdapter(adapter);
        }
    }

    // last search method that tack the last search from database
    public void getLastSearch(){

        Cursor c = mainActivity.getContentResolver().query(PlaceProvider.CONTENT_URI, null, null, null, null);

        while (c.moveToNext()) {

            long id = c.getLong(c.getColumnIndex(PlaceDBHelper.COL_ID));
            String placeId = c.getString(c.getColumnIndex(PlaceDBHelper.COL_PLACE_ID));
            String name = c.getString(c.getColumnIndex(PlaceDBHelper.COL_NAME));
            String address = c.getString(c.getColumnIndex(PlaceDBHelper.COL_ADDRESS));
            String image = c.getString(c.getColumnIndex(PlaceDBHelper.COL_IMAGE));
            double lat = c.getDouble(c.getColumnIndex(PlaceDBHelper.COL_LATITUDE));
            double lng = c.getDouble(c.getColumnIndex(PlaceDBHelper.COL_LONGITUDE));
            String distance = c.getString(c.getColumnIndex(PlaceDBHelper.COL_DISTANCE));

            places.add(new Place(name, address, image, lat, lng, id, placeId, distance));
        }

        adapter.setPlaces(places);
        placesList.setAdapter(adapter);
    }

    // insert place into database method
    public void insertPlaceIntoDB(Place place){

        ContentValues values = new ContentValues();
        values.put(PlaceDBHelper.COL_PLACE_ID, place.getPlaceId());
        values.put(PlaceDBHelper.COL_NAME, place.getName());
        values.put(PlaceDBHelper.COL_ADDRESS, place.getAddress());
        values.put(PlaceDBHelper.COL_IMAGE, place.getImage());
        values.put(PlaceDBHelper.COL_LATITUDE, place.getLatitude());
        values.put(PlaceDBHelper.COL_LONGITUDE, place.getLongitude());
        values.put(PlaceDBHelper.COL_DISTANCE, place.getDistance());

        mainActivity.getContentResolver().insert(PlaceProvider.CONTENT_URI, values);
    }

    // create the progress dialog method
    public void loadingProgress(final int sleepTime, String title, int results){

        progressDialog = new ProgressDialog(mainActivity);
        progressDialog.setMax(results);
        progressDialog.setMessage("Loading...");
        progressDialog.setTitle(title);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (progressDialog.getProgress() <= progressDialog.getMax()) {

                        Thread.sleep(sleepTime);
                        handle.sendMessage(handle.obtainMessage());
                        if (progressDialog.getProgress() == progressDialog.getMax()){
                            progressDialog.dismiss();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    Handler handle = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            progressDialog.incrementProgressBy(1);
        }
    };

    // close dialog method
    public void dismissProgress(){
        progressDialog.dismiss();
    }
}
