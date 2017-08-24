package com.example.sunny.androidproject2;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Sunny on 17/05/2017.
 */

public class JsonService extends IntentService {

    public static final String ACTION_JSON = "com.example.sunny.androidproject2.action.GET_JSON";


    public JsonService() {
        super("JsonService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        HttpsURLConnection connection = null;
        BufferedReader reader = null;
        String result = "";

        // connect between the reader to url info
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

            // send the json to who's called you
            Intent sendIntent = new Intent(ACTION_JSON);
            sendIntent.putExtra("json", result);
            LocalBroadcastManager.getInstance(this).sendBroadcast(sendIntent);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

