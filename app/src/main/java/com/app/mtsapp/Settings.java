package com.app.mtsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    private LanguageManager languageManager;

    private ToggleButton notificationOne, notificationTwo, notificationThree;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        languageManager = new LanguageManager(this);
        languageManager.checkLocale();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button englishButton = findViewById(R.id.englishButton), serbianLatinButton = findViewById(R.id.serbianLatinButton), serbianCyrillicButton = findViewById(R.id.serbianCyrillicButton);
        //Постави језик на енглески
        englishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageManager.setLocale("en", true);
            }
        });
        //Постави језик на српску латиницу
        serbianLatinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageManager.setLocale("sr", true);
            }
        });
        //Постави језик на српску ћирилицу
        serbianCyrillicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageManager.setLocale("sr-rRS", true);
            }
        });

        //Референцирај шерд префс и тогл дугмиће за нотификације
        sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        notificationOne = findViewById(R.id.notificationOne);
        notificationTwo = findViewById(R.id.notificationTwo);
        notificationThree = findViewById(R.id.notificationThree);

        loadEnabledNotifications(); //Учита сачувана стања дугмића за нотификације сачувана у уређају

        //Сачувај промене стања коришћења нотификација у уређају
        notificationOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean("notificationOne", notificationOne.isChecked()).apply();
            }
        });
        notificationTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean("notificationTwo", notificationTwo.isChecked()).apply();
            }
        });
        notificationThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean("notificationThree", notificationThree.isChecked()).apply();
            }
        });
    }

    //Учита сачувана стања дугмића за нотификације сачувана у уређају и промени стања дугмића
    private void loadEnabledNotifications() {
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        notificationOne.setChecked(sharedPreferences.getBoolean("notificationOne", true));
        notificationTwo.setChecked(sharedPreferences.getBoolean("notificationTwo", true));
        notificationThree.setChecked(sharedPreferences.getBoolean("notificationThree", true));
    }

    //Кад корисник притисне дугме за враћање назад на уређају
    @Override
    public void onBackPressed() {
        //Прикажи упозорење
        AlertPopupManager alertPopupManager = new AlertPopupManager(this, getResources().getString(R.string.backToMenu), false);
        alertPopupManager.showAlertDialog();
    }
}