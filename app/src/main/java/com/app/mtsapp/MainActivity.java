package com.app.mtsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mtsapp.location.LocationFinder;

public class MainActivity extends AppCompatActivity{

    private LocationFinder finder;

    private TextView tvLatitude,tvLongitude;
    private Button getLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        finder = new LocationFinder(this);
        tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        getLocation = (Button) findViewById(R.id.getLocation);

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finder.stopLocating();
    }
}