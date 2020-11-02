package com.app.mtsapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.mtsapp.location.LocationFinder;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity{

    private LocationFinder finder;

    private TextView tvLatitude,tvLongitude;

    private TextView googleDetectedText, dailyTipTextView;
    private int PLACE_PICKER_REQUEST = 1;

    //Dnevni saveti
    private String[] dailyTips = {"Tip one", "Tip two", "Tip three", "Tip four", "Tip five"}; //Dnevni saveti koji se prikazuju na glavnom ekranu
    private Random random = new Random(); //Za generisanje nasumicnih brojeva
    private int currentTipIndex; //Indeks trenutnog saveta u nizu saveta, koriscen za menjanje saveta prikazanog pri promeni datuma

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Lock activity into portrait mode

        finder = new LocationFinder(this);
        tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        Button getLocation = (Button) findViewById(R.id.getLocation);

        //Mrmi - otvaranje preko gugl mapa
        Button googleMapsButton = findViewById(R.id.googleMapsButton);
        googleDetectedText = findViewById(R.id.googleDetectedText);

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = finder.getCurrentLocation();
                if(location!=null){
                    tvLatitude.setText("" + location.getLatitude());
                    tvLongitude.setText("" + location.getLongitude());
                }
            }
        });

        googleMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        dailyTipTextView = findViewById(R.id.dailyTipText);
        checkDailyTip();

        Button notifyButton = findViewById(R.id.notifyButton);
        notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationSender notificationSender = new NotificationSender(MainActivity.this);
                notificationSender.showNotification();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK) {
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

    //Proverava da li treba da se promeni prikazani dnevni savet
    private void checkDailyTip() {
        //Referenca SharedPreferences-a: lakog nacina cuvanja prostih podataka
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);

        //Uzmi trenutni dan preko Calendar.getInstance() i nadji poslednji sacuvani dan u SharedPreferences
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH), lastSavedDayD = sharedPreferences.getInt("LastSavedDayD", -1);

        System.out.println("[MRMI]: Today: " + currentDay + " Last saved day: " + lastSavedDayD);

        //Ako ne postoji poslednji dan sacuvan na uredjaju
        if(lastSavedDayD == -1) {
            changeDailyTip(-1); //Prikazi prvi savet iz niza saveta

            //Sacuvaj trenutni dan na uredjaju
            lastSavedDayD = currentDay;
            sharedPreferences.edit().putInt("LastSavedDayD", lastSavedDayD).apply();

            //Takodje sacuvaj indeks trenutnog saveta kako bi se on prikazao pri ponovnom ulazenju u aplikaciju istog dana
            sharedPreferences.edit().putInt("CurrentTipIndex", currentTipIndex).apply();

            System.out.println("[MRMI]: Last day is -1, current tip index: " + currentTipIndex);
        }
        //Ako se sacuvan dan i danas ne poklapaju
        else if(lastSavedDayD!=currentDay) {
            //Sacuvaj trenutni dan na uredjaju
            lastSavedDayD=currentDay;
            sharedPreferences.edit().putInt("LastSavedDayD", lastSavedDayD).apply();

            //Pronadji indeks trenutnog saveta sa uredjaja i
            currentTipIndex = sharedPreferences.getInt("CurrentTipIndex", -1);

            //Promeni prikazan savet u nasumican savet indeksa koji nije trenutni
            changeDailyTip(currentTipIndex);

            //Sacuvaj indeks trenutnog saveta koji promeni funkcija changeDailyTip()
            sharedPreferences.edit().putInt("CurrentTipIndex", currentTipIndex).apply();

            System.out.println("[MRMI]: Last day is " + lastSavedDayD + ", current tip index: " + currentTipIndex);
        }
        //Ako se dani poklapaju
        else {
            currentTipIndex = sharedPreferences.getInt("CurrentTipIndex", random.nextInt(dailyTips.length)); //Nadji indeks trenutnog saveta

            dailyTipTextView.setText(dailyTips[currentTipIndex]); //I prikazi ga

            System.out.println("[MRMI]: Last day is " + lastSavedDayD + ", current tip index: " + currentTipIndex);
        }
    }

    //Menja trenutni savet u zavisnosti od argumenta
    private void changeDailyTip(int skippedTipIndex) {
        int randomIndex=random.nextInt(dailyTips.length); //Indeks nasumicno izabranog saveta
        if(skippedTipIndex!=-1){ //Ako je indeks validan (!= -1) sve dok se ne nadje indeks drugaciji od datog, uzimaj ga nasumicno
            do {
                randomIndex = random.nextInt(dailyTips.length); //Uzmi nasumican indeks
                System.out.println("[MRMI]: Random index: " + randomIndex + " current index: " + skippedTipIndex);
            }while(randomIndex==skippedTipIndex); //Ponavljaj proces sve dok se ne nadje indeks drugaciji od skippedTipIndex (radi izbegavanja ponavljanja saveta uzastopno)
        }

        currentTipIndex = randomIndex; //Promeni indeks trenutnog saveta
        dailyTipTextView.setText(dailyTips[randomIndex]); //Prikazi izabran nasumican savet
    }
}
