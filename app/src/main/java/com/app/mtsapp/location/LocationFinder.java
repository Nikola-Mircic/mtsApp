package com.app.mtsapp.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationFinder implements Runnable {
    private static final String TAG = "LocationFinder";//Tag koji se koristi za ispisivanje

    private final Context context;//Pamti vrednost konteksta kojeg koristi za trazenje lokacije

    private final int LOCATION_PERMISSSION_CODE = 100;

    private FusedLocationProviderClient flpClient;//Objekat koji pokrece pretragu lokacije
    private LocationRequest locationRequest;//Objekat koji salje zahtev za dobijanje lokacije i prosledjuje je do objekta povratnog poziva
    private LocationCallback locationCallback;//Objekat koji obradjuje adresu kada je dobije

    private int interval;
    private int fastInterval;
    private int priority;
    private final String[] permissions;

    private Location currentLocation;//Trenutna lokacija

    private EventHandler eventUpdate;

    private Thread t;//Thread koji pokrece rad findera
    private boolean running;//True ukoliko je finder pokrenut

    public boolean locating;

    public LocationFinder(Context context) {
        this.context = context;//Pamti se gde je LocationFinder kreiran(u kom aktivitiju)
        this.currentLocation = null;//Lokacija jos uvek nije pronadjena
        this.locating = false;
        eventUpdate = null;
        interval = 10 * 1000;
        fastInterval = 5 * 1000;
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    public void start() {
        if (!running) {
            this.t = new Thread(this);//Kreira se nov zaseban proces za LocatinoFinder
            t.start();
            running = true;
        }
    }

    public void stop() {
        if (running) {
            flpClient.removeLocationUpdates(locationCallback);
            flpClient = null;
            locationRequest = null;
            locationCallback = null;
            running = false;
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(this.priority);
        locationRequest.setInterval(this.interval);
        locationRequest.setFastestInterval(this.fastInterval);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                currentLocation = locationResult.getLastLocation();
                if(eventUpdate!=null){
                    eventUpdate.handle(currentLocation);
                }
                super.onLocationResult(locationResult);
            }
        };

        flpClient = LocationServices.getFusedLocationProviderClient(context);
        if (hasAllPermissions()) {
            flpClient.requestLocationUpdates(locationRequest, locationCallback, context.getMainLooper());
        } else {
            if (context instanceof Activity) {
                Activity temp = (Activity) context;
                ActivityCompat.requestPermissions(temp, permissions, LOCATION_PERMISSSION_CODE);
            }
        }
    }

    private boolean hasAllPermissions() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this.context, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public void setOnUpdateEvent(EventHandler event) {
        this.eventUpdate = event;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getFastInterval() {
        return fastInterval;
    }

    public void setFastInterval(int fastInterval) {
        this.fastInterval = fastInterval;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
