package com.app.mtsapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    private LanguageManager languageManager;

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
    }

    //Кад корисник притисне дугме за враћање назад на уређају
    @Override
    public void onBackPressed() {
        //Прикажи упозорење
        AlertPopupManager alertPopupManager = new AlertPopupManager(this, getResources().getString(R.string.backToMenu), false);
        alertPopupManager.showAlertDialog();
    }
}