package com.app.mtsapp.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.location.LocationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.security.Permission;

public class LocationFinder implements Runnable {
    private static final String TAG = "LocationFinder";//Tag koji se koristi za ispisivanje

    private Context context;//Pamti vrednost konteksta kojeg koristi za trazenje lokacije

    private final int LOCATION_PERMISSSION_CODE = 100;

    private FusedLocationProviderClient flpClient;//Objekat koji pokrece pretragu lokacije
    private LocationRequest locationRequest;//Objekat koji salje zahtev za dobijanje lokacije i prosledjuje je do objekta povratnog poziva
    private LocationCallback locationCallback;//Objekat koji obradjuje adresu kada je dobije

    private Location currentLocation;//Trenutna lokacija

    private LocationEventUpdate eventUpdate;

    private Thread t;//Thread koji pokrece rad findera
    private boolean running;//True ukoliko je finder pokrenut

    public boolean locating;

    public LocationFinder(Context context) {
        this.context = context;//Pamti se gde je LocationFinder kreiran(u kom aktivitiju)
        this.currentLocation = null;//Lokacija jos uvek nije pronadjena
        this.locating = false;
        eventUpdate = null;
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

    @Override
    public void run() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                currentLocation = locationResult.getLastLocation();
                if(eventUpdate!=null){
                    eventUpdate.onUpdate(currentLocation);
                }
                super.onLocationResult(locationResult);
            }
        };

        flpClient = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            flpClient.requestLocationUpdates(locationRequest, locationCallback, context.getMainLooper());
        }else{
            if(context instanceof Activity){
                Activity temp = (Activity) context;
                ActivityCompat.requestPermissions(temp, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSSION_CODE);
            }
        }

    }

    public void setOnUpdateEvent(LocationEventUpdate event){
        this.eventUpdate = event;
    }

    public Location getCurrentLocation() {
        return  currentLocation;
    }
}
