package com.app.mtsapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class PlacesActivity extends AppCompatActivity implements OnMapReadyCallback {
    private SupportMapFragment supportMapFragment;
    private FusedLocationProviderClient flpClient;
    private int locationRequestCode = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        //Узми supportMapFragment и обавести кад је мапа спремна
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    //Позвано кад апликација затражи дозволу за нешто
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == locationRequestCode) {
            //Ако је корисник дозволио употребу локације, позови функцију тражења тренутне локације
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }

    }

    //Пронађе тренутну локацију и прикаже је на мапи
    private void getCurrentLocation() {
        try {
            @SuppressLint("MissingPermission") Task<Location> task = flpClient.getLastLocation();

            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(final Location location) {
                    if (location != null) {
                        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude()); //Узми тренутне координате

                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17)); //Зумирај мапу на тренутну локацију
                                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Тренутна локација"); //Постави подешавања маркера
                                googleMap.addMarker(markerOptions); //Постави маркер тренутне локације на мапу

                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Неуспело тражење тренутне локације", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        flpClient = LocationServices.getFusedLocationProviderClient(PlacesActivity.this);

        //Провери да ли апликације има потребне дозволе за детектовање тренутне локације
        if (ActivityCompat.checkSelfPermission(PlacesActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(PlacesActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
        //Ако их нема затражи их
        else {
            ActivityCompat.requestPermissions(PlacesActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        }
    }
}