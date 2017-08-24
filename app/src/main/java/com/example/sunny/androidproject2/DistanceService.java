package com.example.sunny.androidproject2;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class DistanceService extends IntentService {

    public static final String ACTION_DISTANCE = "com.example.sunny.androidproject2.action.GET_DISTANCE";

    public DistanceService() {
        super("DistanceService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // get the info from PlacesListFragment
        String name = intent.getStringExtra("name");
        String address = intent.getStringExtra("address");
        String image = intent.getStringExtra("image");
        String placeId = intent.getStringExtra("placeId");
        double lat = intent.getDoubleExtra("lat", 0);
        double lng = intent.getDoubleExtra("lng", 0);
//        double rating = intent.getDoubleExtra("rating", 0);

        HttpsURLConnection connection = null;
        BufferedReader reader = null;
        String result = "";

        try {
            // put the url(string) that receive in the url object
            URL url = new URL(intent.getStringExtra("url"));
            // open the connection to the url
            connection = (HttpsURLConnection) url.openConnection();

            // check if there is no problem with the connection
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // get the all info from the url
            String line = reader.readLine();
            while (line != null) {
                result += line;
                line = reader.readLine();
            }

            // get the json from distance api
            JSONObject rootObj = new JSONObject(result);
            JSONArray rowsArr = rootObj.getJSONArray("rows");
            JSONObject rowObj = rowsArr.getJSONObject(0);
            JSONArray elementsArr = rowObj.getJSONArray("elements");
            JSONObject elementObj = elementsArr.getJSONObject(0);
            JSONObject distanceObj = elementObj.getJSONObject("distance");
            String distance = distanceObj.getString("value");

            // send the all info back to PlacesListFragment
            Intent sendIntent = new Intent(ACTION_DISTANCE);
            sendIntent.putExtra("name", name);
            sendIntent.putExtra("address", address);
            sendIntent.putExtra("image", image);
            sendIntent.putExtra("lat", lat);
            sendIntent.putExtra("lng", lng);
//            sendIntent.putExtra("rating", rating);
            sendIntent.putExtra("placeId", placeId);
            sendIntent.putExtra("distance", distance);
            LocalBroadcastManager.getInstance(this).sendBroadcast(sendIntent);


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            // at finally close the reader and the connection
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
