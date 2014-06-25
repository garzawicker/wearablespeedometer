package com.dambrisco.wearablespeedometer;

import android.app.Application;
import android.content.IntentFilter;

/**
 * Created by dambrisco on 2014-06-20.
 */
public class DApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(new StopReceiver(), new IntentFilter(StopReceiver.BROADCAST_ACTION));
    }
}
