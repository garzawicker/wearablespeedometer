package com.dambrisco.wearablespeedometer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
    TextView mView;

    Intent mIntent;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mView.setText(String.format("%d", intent.getIntExtra("speed", 0)));
        }
    };

    private BroadcastReceiver stop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mView = (TextView) findViewById(R.id.speed);

        mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                stopService(mIntent);
                finish();
                return true;
            }
        });

        mIntent = new Intent(this, CurrentSpeedService.class);
        startService(mIntent);
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter(CurrentSpeedService.BROADCAST_ACTION);
        registerReceiver(receiver, filter);

        IntentFilter stopFilter = new IntentFilter(StopReceiver.BROADCAST_ACTION);
        registerReceiver(stop, stopFilter);

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        unregisterReceiver(stop);
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
