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
    private String[] notificationDescriptions = {"Дезинфикуј руке!", "Стави маску!", "Опери руке и проветри маску и одећу!"};
    //int notificationId = 02112020; //Id notifikacije - jedinstven za svaku, u nasem slucaju nek se i preklapaju i jednu drugu override-aju

    //Konstruktor
    public NotificationSender(Context contextArg) {
        context = contextArg;

        createNotificationChannel();
    }

    //Napravi kanal za notifikacije - koriscen u Android verzijama >=8
    private void createNotificationChannel() {

        //U verzijama posle Orea (Android 8) napravi kanal za notifikacije
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Coro-No"; //Ime kanala
            String description = "Coro-No notification channel"; //Opis kanala
            int importance = NotificationManager.IMPORTANCE_HIGH; //Vaznost kanala (prioritet u odnosu na druge notifikacije)
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            //Registruj kanal u uredjaju
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if(notificationManager!=null) {
                notificationManager.createNotificationChannel(channel);
            }

        }
    }

    public void showNotification(int notificationId) {
        //Otvori MainActivity kad korisnik pritisne notifikaciju
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        //Podesavanja notifikacije: naslov, tekst, ikonica, prioritet prikazivanja
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("Test title")
                .setContentText(notificationDescriptions[notificationId])
                .setContentIntent(pendingIntent) //Sta se dogodi kad korisnik pritisne notifikaciju
                .setAutoCancel(true) //Izbrisi notifikaciju kad je korisnik pritisne
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
    }
}
