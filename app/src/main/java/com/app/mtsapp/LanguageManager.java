package com.app.mtsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LanguageManager {
    private final Context context;

    public LanguageManager(Context c) {
        context = c;
    }

    //Проверава сачуван Locale у уређају и ставља га на српску ћирилицу ако није сачуван
    public void checkLocale() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        String savedLocaleString = sharedPreferences.getString("locale", null);
        if (savedLocaleString == null) {
            savedLocaleString = "sr-rRS";
        }

        //Постави језик активитија који је направио објекат овога користећи контекст из конструктора
        setLocale(savedLocaleString, false);
    }

    //Промени језик апликације (и писмо у случају српског)
    public void setLocale(String lang, boolean shouldRefresh) {

        Locale selectedLocale;
        SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("locale", lang).apply();
        //Пошто прослеђујемо српску ћирилицу као sr-rRS која није одговарајући Locale користимо Locale("sr", "RS")
        if (lang.equals("sr-rRS"))
            selectedLocale = new Locale("sr", "RS");
        else
            selectedLocale = new Locale(lang);

        //Промени језик и самим тиме values фолдер који ће се користити у апликацији
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = selectedLocale;
        res.updateConfiguration(conf, dm);

        if (shouldRefresh) {
            //Рестартуј екран како би се сачувала промена језика
            Intent refresh = new Intent(context, context.getClass());
            ((Activity) context).finish();
            context.startActivity(refresh);
        }
    }
}
