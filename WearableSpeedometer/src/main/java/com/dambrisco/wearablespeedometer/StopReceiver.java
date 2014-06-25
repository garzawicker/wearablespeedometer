package com.dambrisco.wearablespeedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by dambrisco on 2014-06-20.
 */
public class StopReceiver extends BroadcastReceiver {
    public static String BROADCAST_ACTION = "com.dambrisco.wearablespeedometer.STOP";

    @Override
    public void onReceive(Context context, Intent intent) {
        context.stopService(new Intent(context, CurrentSpeedService.class));
    }
}
