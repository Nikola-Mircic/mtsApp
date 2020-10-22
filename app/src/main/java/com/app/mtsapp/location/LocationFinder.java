package com.app.mtsapp.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationFinder {

    private  AppCompatActivity activity;

    private int LOCATION_PERMISSSION_CODE = 100;

    private FusedLocationProviderClient flpClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    public LocationFinder(AppCompatActivity activity){
        this.activity = activity;

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
            }
        };
    }

    public Location startLocating(){
        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(activity, "Geting location...", Toast.LENGTH_SHORT).show();
            try {
                flpClient.requestLocationUpdates(locationRequest, locationCallback, activity.getMainLooper());
                flpClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        currentLocation = location;
                    }
                });
                flpClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(activity, "Cann't get location...", Toast.LENGTH_SHORT).show();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            Toast.makeText(activity,"Permission needed!!", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(activity,
                                             new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                                             LOCATION_PERMISSSION_CODE);
        }

        System.out.println(currentLocation.getLatitude());
        System.out.println(currentLocation.getLongitude());
        return  currentLocation;
    }

    public void stopLocating(){
        flpClient.removeLocationUpdates(locationCallback);
    }

}
