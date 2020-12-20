package com.app.mtsapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationSender {
    private static String[] notificationDescriptions;
    private static Bitmap[] notificationIcons;
    private final String CHANNEL_ID = "Coro-No";
    private final Context context;

    //Конструктор
    public NotificationSender(Context contextArg) {
        context = contextArg;

        createNotificationChannel();

        notificationDescriptions = context.getResources().getStringArray(R.array.notificationMessages); //Постављање вредности текста обавештења

        //Постављање битмапова великих иконица обавештења
        notificationIcons = new Bitmap[]{
                BitmapFactory.decodeResource(context.getResources(), R.drawable.put_on_mask),
                BitmapFactory.decodeResource(context.getResources(), R.drawable.disinfect_clothes),
                BitmapFactory.decodeResource(context.getResources(), R.drawable.disinfect_clothes)
        };
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

        if (notificationId > 3 || notificationId < 0)
            notificationId = 0;

        //Отвори MainActivity кад корисник притисне нотификацију
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        String notificationTitle = "Coro-No warning:", notificationText = notificationDescriptions[notificationId];

        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        //RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        notificationLayout.setTextViewText(R.id.notificationText, notificationText);
        notificationLayout.setTextViewText(R.id.notificationTitle, notificationTitle);
        notificationLayout.setImageViewBitmap(R.id.notifcationIcon, notificationIcons[notificationId]);

        //Подешавања нотификације
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark) //Мала иконица нотификације (бела са провидном позадином)
                //.setContentTitle(notificationTitle) //Наслов нотификације
                //.setContentText(notificationText) //Садржај нотификације
                .setContentIntent(pendingIntent) //Шта се догоди кад корисник притисне нотификацију
                .setAutoCancel(true) //Избриши нотификацију кад је корисник притисне
                //.setLargeIcon(notificationIcons[notificationId])
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

    public void showNotification(String title , String text) {
        //Отвори MainActivity кад корисник притисне нотификацију
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        //Подешавања нотификације
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent) //Шта се догоди кад корисник притисне нотификацију
                .setAutoCancel(true) //Избриши нотификацију кад је корисник притисне
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(5, builder.build());
    }
}
