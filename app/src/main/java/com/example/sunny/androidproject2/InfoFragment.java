package com.example.sunny.androidproject2;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.CircularArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment implements View.OnClickListener {

    private Gson gson;
    private SharedPreferences sp;
    private SharedPreferences spSettings;
    private Place place;
    private TextView textName;
    private TextView textAddress;
    private TextView textDistance;
    private RatingBar ratingBar;
    private ImageView imageView;
    private String phone;
    private LinearLayout linearLayout;
    private RelativeLayout relativeList;
    private boolean isKilometers;

    public InfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        // get the views from xml
        textName = (TextView) view.findViewById(R.id.textName);
        textAddress = (TextView) view.findViewById(R.id.textAddress);
        ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        textDistance = (TextView) view.findViewById(R.id.textDistance);
        linearLayout = (LinearLayout) view.findViewById(R.id.lineProgress);
        relativeList = (RelativeLayout) view.findViewById(R.id.relativeList);

        // set the progressBat layout visible
        linearLayout.setVisibility(View.VISIBLE);
        // set the info layout invisible
        relativeList.setVisibility(View.GONE);

        // create gson object
        gson = new Gson();
        // get the place json from PlaceAdapter and convert him back to java object
        sp = getActivity().getSharedPreferences("place_json", Context.MODE_PRIVATE);
        String placeJson = sp.getString("placeJson", "");
        place = gson.fromJson(placeJson, Place.class);

        spSettings = PreferenceManager.getDefaultSharedPreferences(getContext());
        isKilometers = spSettings.getBoolean("switch_kms", false);


        // connect to JsonService with Broadcast/Receiver
        JsonPlaceDetails jsonPlaceDetails = new JsonPlaceDetails();
        IntentFilter filter = new IntentFilter(JsonService.ACTION_JSON);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(jsonPlaceDetails, filter);


        // send with intent the url+placeId to JsonService
        try {
            Intent intentService = new Intent(getContext(), JsonService.class);
            intentService.setAction(JsonService.ACTION_JSON);
            intentService.putExtra("url", String.format("https://maps.googleapis.com/maps/api/place/details/json?placeid=%s" +
                    "&key=AIzaSyCuPa7jSImjuIPE-CSDrVE4DZWa9yb0C2Q", place.getPlaceId()));
            getContext().startService(intentService);
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        // set the buttons click
        view.findViewById(R.id.buttonDirection).setOnClickListener(this);
        view.findViewById(R.id.buttonCall).setOnClickListener(this);
        view.findViewById(R.id.buttonSave).setOnClickListener(this);
        view.findViewById(R.id.buttonShare).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        Intent intent;

        // check which button clicked
        switch (v.getId()) {

            // start the map and send to him the place latitude longitude and name
            case R.id.buttonDirection:
                intent = new Intent(getContext(), MapActivity.class);
                intent.putExtra("lat", place.getLatitude());
                intent.putExtra("lng", place.getLongitude());
                intent.putExtra("name", place.getName());
                startActivity(intent);
                break;

            // start the ACTION_CALL and send to him the place phone number
            case R.id.buttonCall:
                intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
                break;

            // save the place to favorites and toast that is saved
            case R.id.buttonSave:
                insertFavoriteIntoDB(place);
                Toast.makeText(getContext(), place.getName() + " saved", Toast.LENGTH_SHORT).show();
                break;

            // start the ACTION_SEND and put inside the text
            case R.id.buttonShare:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String subject = place.getName();
                intent.putExtra(Intent.EXTRA_TEXT, "I like " + place.getName() + "!\n" + place.getAddress());
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                startActivity(Intent.createChooser(intent, "How do you want to share?"));
                break;
        }
    }

    // insert the place into favorites database method
    public void insertFavoriteIntoDB(Place place){

        ContentValues values = new ContentValues();
        values.put(PlaceDBHelper.COL_PLACE_ID, place.getPlaceId());
        values.put(PlaceDBHelper.COL_NAME, place.getName());
        values.put(PlaceDBHelper.COL_ADDRESS, place.getAddress());
        values.put(PlaceDBHelper.COL_IMAGE, place.getImage());
        values.put(PlaceDBHelper.COL_LATITUDE, place.getLatitude());
        values.put(PlaceDBHelper.COL_LONGITUDE, place.getLongitude());
        values.put(PlaceDBHelper.COL_DISTANCE, place.getDistance());
//        values.put(PlaceDBHelper.COL_RATING, place.getRating());

        getContext().getContentResolver().insert(PlaceProvider.CONTENT_FAVORITES_URI, values);
    }

    // receive the place info json from JsonService
    private class JsonPlaceDetails extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra("json");

            if (json.isEmpty()|| json.equals("")) {
                Toast.makeText(context, "Json Exception", Toast.LENGTH_SHORT).show();
            } else {
                try {

                    // get the phone number and the rating for this place with json object
                    JSONObject jsonObject = new JSONObject(json);
                    JSONObject resultObj = jsonObject.getJSONObject("result");
                    phone = resultObj.getString("international_phone_number");
                    double rating = resultObj.getDouble("rating");

                    // set the view with the info
                    textName.setText(place.getName());
                    textAddress.setText(place.getAddress());
                    ratingBar.setRating((float)rating);
                    textDistance.setText(place.getDistance());

                    float distance = 0;

                    // set the views texts
                    try {
                        distance = Float.parseFloat(place.getDistance());
                        if (isKilometers) {

                            distance /= 1000.0f;
                            textDistance.setText(distance + " KMs");

                        } else {

                            distance = ((int) (distance * PlaceAdapter.MILS_DIV)) / 1000.0f;
                            textDistance.setText(distance + " Mils");

                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }

                    // use picasso to load the place's photo
                    Picasso
                            .with(getContext())
                            .load(String.format("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=%s" +
                                    "&key=AIzaSyCuPa7jSImjuIPE-CSDrVE4DZWa9yb0C2Q", place.getImage()))
                            .resize(1500, 800)
                            .centerCrop()
                            .into(imageView);


                } catch (JSONException e) {
                    e.printStackTrace();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
            // set the progressBat layout invisible
            linearLayout.setVisibility(View.GONE);
            // set the info layout visible
            relativeList.setVisibility(View.VISIBLE);
        }
    }
}
