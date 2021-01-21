package com.app.mtsapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static android.content.Context.MODE_PRIVATE;

public class NotificationSender {
    private static String[] notificationDescriptions;
    private static Bitmap[] notificationIcons;
    private static int[] smallNotificationIcons;
    private final String CHANNEL_ID = "Coro-No";
    private final Context context;

    //Конструктор
    public NotificationSender(Context contextArg) {
        context = contextArg;

        createNotificationChannel();

        LanguageManager languageManager = new LanguageManager(context);
        languageManager.checkLocale();

        notificationDescriptions = context.getResources().getStringArray(R.array.notificationMessages); //Постављање вредности текста обавештења

        //Постављање битмапова великих иконица обавештења
        notificationIcons = new Bitmap[]{
                BitmapFactory.decodeResource(context.getResources(), R.drawable.put_on_mask),
                BitmapFactory.decodeResource(context.getResources(), R.drawable.disinfect_clothes),
                BitmapFactory.decodeResource(context.getResources(), R.drawable.disinfect_hands)
        };

        smallNotificationIcons = new int[]{
                R.drawable.mask_small_icon,
                R.drawable.disinfect_small_icon,
                R.drawable.disinfect_hands_small_icon
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

    //Када је дат id намештају се одговарајућа мала и велика иконица и текст за прављење нотификације
    public void showNotification(int notificationId) {
        if (notificationId > 3 || notificationId < 0)
            notificationId = 0;

        //Немој слати обавештење кориснику ако је у подешавањима искључио слање одређеног обавештења
        SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        if ((notificationId == 0 && !sharedPreferences.getBoolean("notificationOne", true)) ||
                (notificationId == 1 && !sharedPreferences.getBoolean("notificationTwo", true)) ||
                (notificationId == 2 && !sharedPreferences.getBoolean("notificationThree", true))) {
            return;
        }

        buildNotification(notificationId, smallNotificationIcons[notificationId], notificationIcons[notificationId], notificationDescriptions[notificationId]);
    }

    public void showNotification(String text) {
        buildNotification(5, R.drawable.app_small_icon, BitmapFactory.decodeResource(context.getResources(), R.drawable.tracker_large_icon), text);
    }

    //Направи и прикаже нотификацију са датим ID-ем, малом и великом иконицом и текстом
    private void buildNotification(int notificationId, int smallIcon, Bitmap largeIcon, String notificationText) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        notificationLayout.setTextViewText(R.id.notificationTitle, context.getResources().getString(R.string.app_name));
        notificationLayout.setTextViewText(R.id.notificationText, notificationText);
        notificationLayout.setImageViewBitmap(R.id.notifcationIcon, largeIcon);

        //Подешавања обавештења
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIcon) //Мала иконица нотификације (бела са провидном позадином)
                .setContentIntent(pendingIntent) //Шта се догоди кад корисник притисне нотификацију
                .setAutoCancel(true) //Избриши нотификацију кад је корисник притисне
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}
