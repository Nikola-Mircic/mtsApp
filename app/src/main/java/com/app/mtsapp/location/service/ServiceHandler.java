package com.app.mtsapp.location.service;

import android.app.Activity;
import android.content.Intent;

import com.app.mtsapp.MainActivity;

import org.jetbrains.annotations.NotNull;

public class ServiceHandler {

    /**Pokrene servis za pracenje korisnikove lokacije.
     * Servis ne pocinje sa radom onog momenta kada je pokrenut posto je potrebno
     * neko vreme da bi zapocelo GPS lociranje
     *
     * @param instance Referenca na neki postojeci activity
     * */
    public static void startTrackingService(@NotNull Activity instance){
        Intent service = new Intent(instance.getApplicationContext(), Tracker.class);
        instance.startService(service);
    }

    /**Zaustavlja servis za pracenje korisnikove lokacije.
     * Moguce je da servis odradi jos jedan apdejt lokacije i nakon gasenja <u>mada
     * se nije desavalo prilikom poslednjih promena</u>
     *
     * @param instance Referenca na neki postojeci activity
     * */
    public static void stopTrackingService(@NotNull Activity instance){
        Intent service = new Intent(instance.getApplicationContext(), Tracker.class);
        instance.stopService(service);
    }
}
