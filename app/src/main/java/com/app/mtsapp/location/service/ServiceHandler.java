package com.app.mtsapp.location.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ServiceHandler {
    public static AppCompatActivity lastActivityInstance = null;

    /**
     * Pokrene servis za pracenje lokcaije korisnika.
     * Sistem je pokrenut na zasebno procesu, jer bi inace ometao normalan rad aplikacije
     * @param ServiceName Ime klase servisa koji se pokrece
     * */
    public static void startService(final Class ServiceName){
       /* Thread t = new Thread(new Runnable() {
            @Override
            public void run() {*/
                Intent service = new Intent(lastActivityInstance, ServiceName);
                ServiceHandler.lastActivityInstance.startService(service);
           /* }
        });
        t.start();*/
    }
}
