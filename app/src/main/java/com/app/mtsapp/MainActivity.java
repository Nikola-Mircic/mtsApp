package com.app.mtsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.mtsapp.location.LocationFinder;
import com.app.mtsapp.location.LocationSystem;
import com.app.mtsapp.location.SavedLocation;

import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private LocationFinder finder;
    private LocationSystem locationSystem;

    private TextView tvLatitude, tvLongitude;

    //Дневни савети
    private TextView dailyTipTextView;
    private String[] dailyTips; //Дневни савети који се приказују
    private Random random = new Random(); //За генерисање насумичних бројева
    private int currentTipIndex; //Индекс тренутног савета у низу савета, коришћен за мењање савета приказаон при промени датума

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Учитај језик активитија
        LanguageManager languageManager = new LanguageManager(MainActivity.this);
        languageManager.checkLocale();
        dailyTips = getResources().getStringArray(R.array.dailyTips);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Закључај екран у portrait mode

        finder = new LocationFinder(this);
        locationSystem = new LocationSystem(this);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        Button getLocation = findViewById(R.id.getLocation);
        Button checkLocations = findViewById(R.id.checkLocation);//Proveri da li ima sacuvanih lokacija
        Button saveLocations = findViewById(R.id.saveLocation);//Sacuva podatke o imenovanim lokacijama u memoriji telefona

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = finder.getCurrentLocation();
                if (location != null) {
                    tvLatitude.setText(String.valueOf(location.getLatitude()));
                    tvLongitude.setText(String.valueOf(location.getLongitude()));
                    SavedLocation temp = new SavedLocation(location);
                    locationSystem.addLocation(temp);
                }
            }
        });

        checkLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationSystem.loadLocations();
                for(SavedLocation sl : locationSystem.getLocations()){
                    Log.i("[Loaded location]", sl.getName()+ " "+
                                                          sl.getLastDate()+" "+
                                                          sl.getAltitude()+" "+
                                                          sl.getLatitude()+" "+
                                                          sl.getLongitude());
                }
            }
        });

        saveLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationSystem.saveLocations();
                Log.i("[Location save]", " Locations saved!!");
            }
        });

        dailyTipTextView = findViewById(R.id.dailyTipText);
        checkDailyTip();

        Button notifyButton = findViewById(R.id.notifyButton);
        notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationSender notificationSender = new NotificationSender(MainActivity.this);
                notificationSender.showNotification(0);
            }
        });

        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationSender notificationSender = new NotificationSender(MainActivity.this);
                notificationSender.showNotification(1);
                Intent placesIntent = new Intent(MainActivity.this, PlacesActivity.class);
                startActivity(placesIntent);
            }
        });

        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Settings.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finder.stopLocating();
    }

    //Кад корисник притисне дугме за враћање назад на уређају
    @Override
    public void onBackPressed() {
        //Прикажи упозорење
        AlertPopupManager alertPopupManager = new AlertPopupManager(this, getResources().getString(R.string.quitApp), true);
        alertPopupManager.showAlertDialog();
    }

    //Проверава да ли треба да се промени приказани дневни савет
    private void checkDailyTip() {
        //Референца SharedPreferences-a: лаког начина чувања простих података
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);

        //Узми тренутни дан преко и нађи последњи сачувани дан у уређају
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH), lastSavedDayD = sharedPreferences.getInt("LastSavedDayD", -1);

        System.out.println("[MRMI]: Today: " + currentDay + " Last saved day: " + lastSavedDayD);

        //Ако не постоји последњи дан сачуван у уређају
        if(lastSavedDayD == -1) {
            changeDailyTip(-1); //Прикажи насумичан савет из низа савета

            //Сачувај тренутни дан на уређају
            lastSavedDayD = currentDay;
            sharedPreferences.edit().putInt("LastSavedDayD", lastSavedDayD).apply();

            //Такође сачувај индекс тренутног савета како би се он приказао при поноцном улажењу у апликацију истог дана
            sharedPreferences.edit().putInt("CurrentTipIndex", currentTipIndex).apply();

            System.out.println("[MRMI]: Last day is -1, current tip index: " + currentTipIndex);
        }
        //Ако се сачуван дан и данас не поклапају
        else if(lastSavedDayD!=currentDay) {
            //Сачувај тренутни дан у уређају
            lastSavedDayD=currentDay;
            sharedPreferences.edit().putInt("LastSavedDayD", lastSavedDayD).apply();

            //Пронађи индекс тренутног савета са уређаја
            currentTipIndex = sharedPreferences.getInt("CurrentTipIndex", -1);

            //Промени приказан савет у насумичан савет индекса који није тренутни
            changeDailyTip(currentTipIndex);

            //Сачувај индекс тренутног савета који промени функција changeDailyTip()
            sharedPreferences.edit().putInt("CurrentTipIndex", currentTipIndex).apply();

            System.out.println("[MRMI]: Last day is " + lastSavedDayD + ", current tip index: " + currentTipIndex);
        }
        //Ако се дани поклапају
        else {
            currentTipIndex = sharedPreferences.getInt("CurrentTipIndex", random.nextInt(dailyTips.length)); //Нађи индекс тренутног савета у низу савета

            dailyTipTextView.setText(dailyTips[currentTipIndex]); //Прикажи савет

            System.out.println("[MRMI]: Last day is " + lastSavedDayD + ", current tip index: " + currentTipIndex);
        }
    }

    //Мења тренутни савет у зависности од аргумента функције
    private void changeDailyTip(int skippedTipIndex) {
        int randomIndex = random.nextInt(dailyTips.length); //Индекс насумично изабарног савета
        if (skippedTipIndex != -1) { //Ако је индекс валидан (!= -1) све док се не нађе индекс другачији од датог, узимај га насумично
            do {
                randomIndex = random.nextInt(dailyTips.length); //Узми насумичан индекс
                System.out.println("[MRMI]: Random index: " + randomIndex + " current index: " + skippedTipIndex);
            } while (randomIndex == skippedTipIndex); //Понављај процес све док се не нађе индекс другачији од skippedTipIndex (ради избегавања понављања савета)
        }

        currentTipIndex = randomIndex; //Промени индекс тренутног савета
        dailyTipTextView.setText(dailyTips[randomIndex]); //Прикажи изабран насумичан савет
    }
}
