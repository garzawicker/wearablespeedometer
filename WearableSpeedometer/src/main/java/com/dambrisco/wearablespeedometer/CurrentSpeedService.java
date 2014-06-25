package com.dambrisco.wearablespeedometer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preview.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Created by dambrisco on 2014-06-19.
 */
public class CurrentSpeedService extends Service {
    final static String TAG = CurrentSpeedService.class.getSimpleName();
    final static boolean DEMO = false;

    public static String BROADCAST_ACTION = "com.dambrisco.wearablespeedometer.UPDATE";

    NotificationManagerCompat mManager;
    Notification.Builder mBuilder;
    Notification mNotification;
    Notification.BigPictureStyle mStyle;

    int mSpeed = 0;
    int mNotificationID = 100;
    long mStartTime;

    Thread mThread;

    LinkedList<Location> mLocationQueue = new LinkedList<Location>();
    final double milesToMeters = 1609.34;
    final long hoursToNanos = 3600000000000L;

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, String.format("%s, %s", location.getLatitude(), location.getLongitude()));
            double speed = location.getSpeed();
            if (speed == 0.0 && mLocationQueue.size() > 0) {
                Location previousLocation = mLocationQueue.pop();
                long meters = calculateDistance(location, previousLocation);
                double miles = (double) meters / milesToMeters;
                long elapsedTime = location.getElapsedRealtimeNanos() - previousLocation.getElapsedRealtimeNanos();
                double hours = (double) elapsedTime / (double) hoursToNanos;
                mSpeed = (int) Math.round(miles / hours);
            } else {
                mSpeed = (int) Math.round(speed);
            }
            mLocationQueue.push(location);
            renewNotification();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, String.format("%s: %s", provider, status));
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), mNotificationID, new Intent(StopReceiver.BROADCAST_ACTION), PendingIntent.FLAG_CANCEL_CURRENT);

        mStartTime = System.currentTimeMillis() * 2;
        mStyle = new Notification.BigPictureStyle();
        mStyle.bigPicture(BitmapFactory.decodeResource(getResources(), getResourceForSpeed(mSpeed)));
        mManager = NotificationManagerCompat.from(this);
        mBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(String.format("%d mph", mSpeed))
                .setContentText("Current speed")
                .setPriority(Notification.PRIORITY_MAX)
                .setShowWhen(false)
                .addAction(R.drawable.ic_launcher, "Stop", pending)
                .setStyle(mStyle)
                .setWhen(mStartTime);
        mNotification = mBuilder.build();

        updateSpeed();
    }

    @Override
    public void onDestroy() {
        mManager.cancel(mNotificationID);
        if (mThread != null)
            mThread.interrupt();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void renewNotification() {
        mManager = NotificationManagerCompat.from(this);
        mBuilder.setContentTitle(String.format("%d mph", mSpeed));
        mStyle.bigPicture(BitmapFactory.decodeResource(getResources(), getResourceForSpeed(mSpeed)));
        mBuilder.setWhen(System.currentTimeMillis() * 2);
        mNotification = mBuilder.build();
        mManager.notify(mNotificationID, mNotification);

        broadcast();
    }

    public void broadcast() {
        Intent broadcast = new Intent();
        broadcast.setAction(BROADCAST_ACTION);
        broadcast.putExtra("speed", mSpeed);
        sendBroadcast(broadcast);
    }

    private void runDemo() {
        try {
            while (mSpeed < 61) {
                mSpeed++;
                renewNotification();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Log.d(TAG, "Ending monitor loop");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runRelease() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = LocationManager.GPS_PROVIDER;

        locationManager.requestLocationUpdates(provider, 0, 0, mLocationListener);
        Location lastLocation = locationManager.getLastKnownLocation(provider);
        mLocationQueue.push(lastLocation);
    }

    private void updateSpeed() {
        if (DEMO) {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runDemo();
                }
            });
            mThread.start();
        }
        else
            runRelease();
    }

    private static long calculateDistance(Location recent, Location prior) {
        return calculateDistance(recent.getLatitude(),
                recent.getLongitude(),
                prior.getLatitude(),
                prior.getLongitude());
    }

    private static long calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        long distanceInMeters = Math.round(6371000 * c);
        return distanceInMeters;
    }

    private int getResourceForSpeed(int speed) {
        speed = speed > 60 ? 60 : speed;
        speed = speed < 0 ? 0 : speed;
        String fieldName = String.format("speedometer_%02d", speed);
        try {
            Field field = R.drawable.class.getDeclaredField(fieldName);
            return field.getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
