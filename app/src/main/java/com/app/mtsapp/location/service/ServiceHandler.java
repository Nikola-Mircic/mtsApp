package com.app.mtsapp.location.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;

import com.app.mtsapp.NotificationBroadcast;

import org.jetbrains.annotations.NotNull;

public class ServiceHandler {
    public static int activityTest = 0;
    /**Pokrene servis za pracenje korisnikove lokacije.
     * Servis ne pocinje sa radom onog momenta kada je pokrenut posto je potrebno
     * neko vreme da bi zapocelo GPS lociranje
     *
     * @param instance Referenca na neki postojeci activity
     * */
    public static void startTrackingService(@NotNull Activity instance) {
        SharedPreferences sharedPreferences = instance.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("trackerRunning", false)) {
            return;
        }
        sharedPreferences.edit().putBoolean("trackerRunning", true).apply();
        sharedPreferences.edit().putBoolean("trackerSwitch", true).apply();
        Intent service = new Intent(instance.getApplicationContext(), Tracker.class);
        instance.startService(service);
    }

    /**Zaustavlja servis za pracenje korisnikove lokacije.
     * Moguce je da servis odradi jos jedan apdejt lokacije i nakon gasenja <u>mada
     * se nije desavalo prilikom poslednjih promena</u>
     *
     * @param instance Referenca na neki postojeci activity
     * */
    public static void stopTrackingService(@NotNull Activity instance) {
        SharedPreferences sharedPreferences = instance.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("trackerRunning", false).apply();
        sharedPreferences.edit().putBoolean("trackerSwitch", false).apply();
        Intent service = new Intent(instance.getApplicationContext(), Tracker.class);
        instance.stopService(service);
    }

    public static void startDailyNotification(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        Intent temp = new Intent(context, NotificationBroadcast.class);
        temp.setPackage("com.app.mtsapp");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, temp, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, SystemClock.uptimeMillis(), pendingIntent);
        }

        sharedPreferences.edit().putBoolean("sendDailyNotifications", true).apply();
    }

    public static void stopDailyNotification(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        Intent temp = new Intent(context, NotificationBroadcast.class);
        temp.setPackage("com.app.mtsapp");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, temp, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);

        sharedPreferences.edit().putBoolean("sendDailyNotifications", false).apply();
    }
}
