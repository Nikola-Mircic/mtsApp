package com.app.mtsapp.location.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

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
        if (sharedPreferences.getInt("trackerRunning", -1) == 1) {
            Toast.makeText(instance, "Service is already running...", Toast.LENGTH_SHORT).show();
            return;
        }
        sharedPreferences.edit().putInt("trackerRunning", 1).apply();
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
        sharedPreferences.edit().putInt("trackerRunning", 0).apply();
        sharedPreferences.edit().putBoolean("trackerSwitch", false).apply();
        Intent service = new Intent(instance.getApplicationContext(), Tracker.class);
        instance.stopService(service);
    }
}
