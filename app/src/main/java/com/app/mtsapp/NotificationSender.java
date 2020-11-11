package com.app.mtsapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationSender {
    private String CHANNEL_ID = "Coro-No";
    private Context context;
    private String[] notificationDescriptions;

    //Конструктор
    public NotificationSender(Context contextArg) {
        context = contextArg;
        notificationDescriptions = context.getResources().getStringArray(R.array.notificationMessages);
        createNotificationChannel();
    }

    //Направи канал за нотификације - коришћен у Андроид верзијама >=8
    private void createNotificationChannel() {

        //У верзијама после Ореа (Андроид 8), направи канал за нотификације
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Coro-No"; //Име канала
            String description = "Coro-No notification channel"; //Опис канала
            int importance = NotificationManager.IMPORTANCE_HIGH; //Важност канала (приоритет у односу на друге нотификације)
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            //Региструј канал у уређају
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void showNotification(int notificationId) {
        //Отвори MainActivity кад корисник притисне нотификацију
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        //Подешавања нотификације
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("Test title")
                .setContentText(notificationDescriptions[notificationId])
                .setContentIntent(pendingIntent) //Шта се догоди кад корисник притисне нотификацију
                .setAutoCancel(true) //Избриши нотификацију кад је корисник притисне
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(notificationId, builder.build());
    }
}
