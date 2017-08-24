package com.example.sunny.androidproject2;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView favoritesList;
    private PlaceAdapter adapter;
    private ArrayList<Place> favorites;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        // create adapter and array list
        adapter = new PlaceAdapter(getContext(), null);
        favorites = new ArrayList<>();

        // get the view from xml
        favoritesList = (RecyclerView) view.findViewById(R.id.favoritesList);
        favoritesList.setLayoutManager(new LinearLayoutManager(getContext()));
        // connect between xml list to adapter
        favoritesList.setAdapter(adapter);

        try {
            // try to read the favorites from database
            getActivity().getSupportLoaderManager().initLoader(1, null, this);
        }catch (Exception e){
            Log.d("Favorite db", "Can't read information from favorites table");
        }

        return view;
    }

    // delete all favorites method
    public void deleteFavorites(){

        favorites.clear();
        adapter.setPlaces(favorites);
        favoritesList.setAdapter(adapter);
        getContext().getContentResolver().delete(PlaceProvider.CONTENT_FAVORITES_URI, null, null);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), PlaceProvider.CONTENT_FAVORITES_URI, null, null, null, null);
    }

    // reade the all info from database method
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        favorites.clear();
        while (data.moveToNext()) {

            long id = data.getLong(data.getColumnIndex(PlaceDBHelper.COL_ID));
            String placeId = data.getString(data.getColumnIndex(PlaceDBHelper.COL_PLACE_ID));
            String name = data.getString(data.getColumnIndex(PlaceDBHelper.COL_NAME));
            String address = data.getString(data.getColumnIndex(PlaceDBHelper.COL_ADDRESS));
            String image = data.getString(data.getColumnIndex(PlaceDBHelper.COL_IMAGE));
            double lat = data.getDouble(data.getColumnIndex(PlaceDBHelper.COL_LATITUDE));
            double lng = data.getDouble(data.getColumnIndex(PlaceDBHelper.COL_LONGITUDE));
            String distance = data.getString(data.getColumnIndex(PlaceDBHelper.COL_DISTANCE));

            Place place = new Place(name, address, image, lat, lng, id, placeId, distance);
            favorites.add(place);
        }
        adapter.setPlaces(favorites);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
