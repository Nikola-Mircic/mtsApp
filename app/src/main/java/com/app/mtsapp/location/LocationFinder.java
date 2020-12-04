package com.app.mtsapp.location;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.mtsapp.NotificationSender;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationFinder implements Runnable {
    private static final String TAG = "LocationFinder";//Tag koji se koristi za ispisivanje

    private AppCompatActivity activity;//Pamti vrednost aktivitija koji koristi location finder

    private final int LOCATION_PERMISSSION_CODE = 100;//

    private FusedLocationProviderClient flpClient;//Objekat koji pokrece pretragu lokacije
    private LocationRequest locationRequest;//Objekat koji salje zahtev za dobijanje lokacije i prosledjuje je do objekta povratnog poziva
    private LocationCallback locationCallback;//Objekat koji obradjuje adresu kada je dobije

    private Location currentLocation;//Trenutna lokacija

    private Thread t;//Thread koji pokrece rad findera
    private boolean running;//True ukoliko je finder pokrenut

    public LocationFinder(AppCompatActivity activity) {
        this.activity = activity;//Pamti se gde je LocationFinder kreiran(u kom aktivitiju)
        this.currentLocation = null;//Lokacija jos uvek nije pronadjena
    }

    public synchronized void start() {
        if (!running) {
            this.t = new Thread(this);//Kreira se nov zaseban proces za LocatinoFinder
            t.start();
            running = true;
        }
    }

    public synchronized void stop() {
        if (running) {
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

    @Override
    public void run() {
        flpClient = LocationServices.getFusedLocationProviderClient(this.activity);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };

        while (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSSION_CODE);
        }

        startLocating();
    }

    public void startLocating() {
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                flpClient.requestLocationUpdates(locationRequest, locationCallback, activity.getMainLooper());
                flpClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        currentLocation = location;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Location getCurrentLocation() {
        if (running) {
            if (currentLocation == null) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return flpClient.getLastLocation().getResult();
                }
            }
        }
        return  currentLocation;
    }

    public Activity getActivity() {
        return activity;
    }

    public void stopLocating(){
        flpClient.removeLocationUpdates(locationCallback);
    }
}
