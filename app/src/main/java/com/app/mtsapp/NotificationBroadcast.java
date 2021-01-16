package com.app.mtsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Random;

public class NotificationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String[] dailyTips = context.getResources().getStringArray(R.array.dailyTips);
        SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        int currentTipIndex = sharedPreferences.getInt("CurrentTipIndex", -1);

        changeDailyTip(currentTipIndex, context, dailyTips);
    }

    //Мења тренутни савет у зависности од аргумента функције
    private void changeDailyTip(int skippedTipIndex, Context context, String[] dailyTips) {
        Random random = new Random();
        int randomIndex = random.nextInt(dailyTips.length); //Индекс насумично изабарног савета
        if (skippedTipIndex != -1) { //Ако је индекс валидан (!= -1) све док се не нађе индекс другачији од датог, узимај га насумично
            while (randomIndex == skippedTipIndex) {
                randomIndex = random.nextInt(dailyTips.length); //Узми насумичан индекс
            }
        }

        NotificationSender sender = new NotificationSender(context);
        sender.showNotification("Coro-No", dailyTips[randomIndex]);
    }
}
