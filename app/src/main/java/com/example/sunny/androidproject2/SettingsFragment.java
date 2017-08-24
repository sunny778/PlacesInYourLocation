package com.example.sunny.androidproject2;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {


    private boolean isKilometers = true;
    private SwitchPreference switchKms;
    private SwitchPreference switchMils;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        addPreferencesFromResource(R.xml.settings);

        // create the SharedPreferences
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // get the switchPreference from xml
        switchKms = (SwitchPreference) findPreference("switch_kms");
        // set the OnPreferenceChangeListener on switch_kms
        switchKms.setOnPreferenceChangeListener(this);
        // get the last switch status
        isKilometers = sp.getBoolean("switch_kms", false);

        // get the switchPreference from xml
        switchMils = (SwitchPreference) findPreference("switch_mils");
        // set the OnPreferenceChangeListener on switch_mils
        switchMils.setOnPreferenceChangeListener(this);
        // get the last switch status
        isKilometers = sp.getBoolean("switch_mils", true);

        // get the editTextPreference from xml
        EditTextPreference editRadiusPreference = (EditTextPreference) findPreference("edit_text_preference");
        // set the OnPreferenceChangeListener on edit_text_preference
        editRadiusPreference.setOnPreferenceChangeListener(this);
        // get the last radius
        String userRadius = sp.getString("edit_text_preference", PlacesListFragment.DEFAULT_RADIUS);
        // put the radius "number" into summary
        editRadiusPreference.setSummary(userRadius);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        switch (preference.getKey()){

            case "edit_text_preference":

            // make the calculate to convert between mils to kilometers and save the radius
                String userRadius = String.valueOf(newValue);
                if ((Integer.parseInt(userRadius)) > (Integer.parseInt(PlacesListFragment.MAX_RADIUS)) ){
                    Toast.makeText(getActivity(), "Sorry you have to insert a number under " + PlacesListFragment.MAX_RADIUS, Toast.LENGTH_SHORT).show();
                    userRadius = PlacesListFragment.MAX_RADIUS;
                }else if ((Integer.parseInt(userRadius)) < (Integer.parseInt(PlacesListFragment.MIN_RADIUS))){
                    Toast.makeText(getActivity(), "Sorry you have to insert a number over " + PlacesListFragment.MIN_RADIUS, Toast.LENGTH_SHORT).show();
                    userRadius = PlacesListFragment.MIN_RADIUS;
                }
                preference.setSummary(userRadius);
                break;

            case "switch_mils":
            case "switch_kms":
                // change the switch every click (on both)
                switchKms.setChecked(isKilometers);
                switchMils.setChecked(!isKilometers);
                // change the boolean isKilometers to opposite
                isKilometers = !isKilometers;

                break;
        }
        return true;
    }
}
