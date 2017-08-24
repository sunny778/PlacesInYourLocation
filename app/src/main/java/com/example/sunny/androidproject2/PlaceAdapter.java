package com.example.sunny.androidproject2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

/**
 * Created by jbt on 11/05/2017.
 */

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceHolder>{

    protected static final double MILS_DIV = 0.6;

    private Context context;
    private ArrayList<Place> places;
    private SharedPreferences spInfo;
    private Gson gson;
    private SharedPreferences spSettings;

    // adapter ctor
    public PlaceAdapter(Context context, ArrayList<Place> places) {
        this.context = context;
        this.places = places;
    }

    // array list<Place> setter
    public void setPlaces(ArrayList<Place> places) {
        this.places = places;
        notifyDataSetChanged();
    }

    @Override
    public PlaceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.place_style, parent, false);

        spSettings = PreferenceManager.getDefaultSharedPreferences(context);

        // create the SharedPreferences
        spInfo = context.getSharedPreferences("place_json", Context.MODE_PRIVATE);

        // create gson object
        gson = new Gson();
        return new PlaceHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaceHolder holder, int position) {
        holder.bind(places.get(position));
    }

    @Override
    public int getItemCount() {
        if (places != null) {
            return places.size();
        }
        return 0;
    }


    public class PlaceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Place place;
        private TextView textName;
        private TextView textAddress;
        private TextView textDistance;
        private ImageView placeImage;
        private float distance = 0.0f;
        private boolean isKilometers;

        public PlaceHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            // get the views from xml
            textName = (TextView) itemView.findViewById(R.id.textName);
            textAddress = (TextView) itemView.findViewById(R.id.textAddress);
            placeImage = (ImageView) itemView.findViewById(R.id.placeImage);
            textDistance = (TextView) itemView.findViewById(R.id.textDistance);
        }

        public void bind(Place bindPlace) {
            this.place = bindPlace;

            // get the boolean from settings to know if user choose KMs or Mils
            isKilometers = spSettings.getBoolean("switch_kms", false);

            try {
                distance = Float.parseFloat(place.getDistance());
                if (isKilometers) {

                    distance /= 1000.0f;
                    textDistance.setText(String.format("%.2f", distance) + " KMs");

                } else {

                    distance = ((int) (distance * MILS_DIV)) / 1000.0f;
                    textDistance.setText(String.format("%.2f", distance) + " Mils");

                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            // set the views texts
            textName.setText(place.getName());
            textAddress.setText(place.getAddress());


            // use picasso to load the place's photo
            Picasso
                    .with(context)
                    .load(String.format("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=%s" +
                            "&key=AIzaSyCuPa7jSImjuIPE-CSDrVE4DZWa9yb0C2Q", place.getImage()))
                    .resize(1500 ,300)
                    .centerCrop()
                    .into(placeImage);

        }

        @Override
        public void onClick(View v) {

            // convert the place object to gson
            String placeJson = gson.toJson(place);
            // use the SharedPreferences to share the gson place
            spInfo.edit().putString("placeJson", placeJson).commit();
            // use the MainActivity method to change to InfoFragment
            ((MainActivity) context).changeToInfoFragment();

        }
    }
}
