package com.example.sunny.androidproject2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;

/**
 * Created by Sunny on 01/06/2017.
 */

public class BatteryCharge extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // get the status from the battery sensor
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                // battery charging
                Toast.makeText(context, "Battery is Charging ",Toast.LENGTH_SHORT).show();
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                // battery full
                Toast.makeText(context, "Battery is Full ",Toast.LENGTH_SHORT).show();
                break;

        }
    }
}
