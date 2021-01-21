package com.app.mtsapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Random;

public class NotificationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LanguageManager languageManager = new LanguageManager(context);
        languageManager.checkLocale();

        String[] dailyTips = context.getResources().getStringArray(R.array.dailyTips);
        SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        int currentTipIndex = sharedPreferences.getInt("CurrentTipIndex", -1);

        changeDailyTip(currentTipIndex, context, dailyTips);

        Intent temp = new Intent(context, NotificationBroadcast.class);
        temp.setPackage("com.app.mtsapp");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, temp, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        long trigerMilis = c.getTimeInMillis()+AlarmManager.INTERVAL_DAY;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigerMilis, pendingIntent);
        }
    }

    //Мења тренутни савет у зависности од аргумента функције
    private void changeDailyTip(int skippedTipIndex, Context context, String[] dailyTips) {
        Random random = new Random();
        int tipIndex;
        if (skippedTipIndex == -1) { //Ако је индекс валидан (!= -1) све док се не нађе индекс другачији од датог, узимај га насумично
            tipIndex = random.nextInt(dailyTips.length);
        }else{
            tipIndex = skippedTipIndex;
        }

        NotificationSender sender = new NotificationSender(context);
        sender.showNotification(dailyTips[tipIndex]);
    }
}
