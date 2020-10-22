package com.app.mtsapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mtsapp.location.LocationFinder;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

public class MainActivity extends AppCompatActivity{

    private LocationFinder finder;

    private TextView tvLatitude,tvLongitude;
    private Button getLocation;

    //Mrmi - otvaranje preko gugl mapa
    private Button googleMapsButton;
    private TextView googleDetectedText;
    private int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        finder = new LocationFinder(this);
        tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        getLocation = (Button) findViewById(R.id.getLocation);

        googleMapsButton = findViewById(R.id.googleMapsButton);
        googleDetectedText = findViewById(R.id.googleDetectedText);

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    finder.startLocating();
                    Location location = finder.getCurrentLocation();
                    tvLatitude.setText("" + location.getLatitude());
                    tvLongitude.setText("" + location.getLongitude());
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        googleMapsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PLACE_PICKER_REQUEST)
        {
            if(resultCode == RESULT_OK)
            {
                Place place = PlacePicker.getPlace(this, data);
                String text;
                String latitude = String.valueOf(place.getLatLng().latitude);
                String longitude = String.valueOf(place.getLatLng().longitude);
                text = "LATITUDE: " + latitude + "\nLONGITUDE: " + longitude;

                googleDetectedText.setText(text);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finder.stopLocating();
    }
}