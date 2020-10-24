package com.app.mtsapp.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationFinder implements Runnable{
    private static final String TAG = "LocationFinder";//Tag koji se koristi za ispisivanje
    private  AppCompatActivity activity;//Pamti vrednost aktivitija koji koristi location finder

    private final int LOCATION_PERMISSSION_CODE = 100;//

    private FusedLocationProviderClient flpClient;//Objekat koji pokrece pretragu lokacije
    private LocationRequest locationRequest;//Objekat koji salje zahtev za dobijanje lokacije i prosledjuje je do objekta povratnog poziva
    private LocationCallback locationCallback;//Objekat koji obradjuje adresu kada je dobije

    private Location currentLocation;//Trenutna lokacija

    public LocationFinder(AppCompatActivity activity){
        this.activity = activity;//Pamti se gde je LocationFinder kreiran(u kom aktivitiju)
        this.currentLocation = null;//Lokacija jos uvek nije pronadjena

        Thread t = new Thread(this);//Kreira se nov zaseban proces za LocatinoFinder
        t.start();
    }

    @Override
    public void run() {
        flpClient = LocationServices.getFusedLocationProviderClient(this.activity);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        locationCallback = new LocationCallback(){
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

        for(int i=0;i<20;++i) {
            Log.d(TAG, "sleeping...: "+i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        startLocating();
    }

    public void startLocating(){
        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            try {
                flpClient.requestLocationUpdates(locationRequest, locationCallback, activity.getMainLooper());
                /*flpClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        currentLocation = location;
                    }
                });*/
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            Toast.makeText(activity, "Permission needed!!", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSSION_CODE);
        }
    }

    public Location getCurrentLocation(){
        return  currentLocation;
    }

    public void stopLocating(){
        flpClient.removeLocationUpdates(locationCallback);
    }

}
