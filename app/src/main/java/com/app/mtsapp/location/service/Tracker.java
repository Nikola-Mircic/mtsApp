package com.app.mtsapp.location.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.app.mtsapp.LanguageManager;
import com.app.mtsapp.NotificationSender;
import com.app.mtsapp.PlacesActivity;
import com.app.mtsapp.R;
import com.app.mtsapp.location.EventHandler;
import com.app.mtsapp.location.LocationFinder;
import com.app.mtsapp.location.LocationSystem;
import com.app.mtsapp.location.SavedLocation;
import com.google.android.gms.location.LocationRequest;

import java.util.List;

public class Tracker extends Service {
    private LocationFinder finder;

    private SavedLocation lastLocation;
    private SavedLocation lastVisitedLocation;

    //Референца SharedPreferences-a: лаког начина чувања простих података, овде због слања адекватне нотификације при промени локације
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);

        //Program kreira odvojeni kanal za notifikacije poslate sa servisa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("trackingchannel", "TrackChannel", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Context context = this.getApplicationContext();//Ova linija je tu da malo ulepsa kod ( da ne pozivam stalno ovu funkciju)

        //Провери учитан језик апликације због текста нотификације
        LanguageManager languageManager = new LanguageManager(this);
        languageManager.checkLocale();

        //Подешавања нотификације која се не склања
        String notificationText = getResources().getString(R.string.serviceNotificationText); //Наслов
        Intent nIntent = new Intent(context, PlacesActivity.class); //Упали екран са мапом када се стисне нотификација
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, nIntent, 0);

        //Намести изглед користећи notification_layout - наслов, текст, велика иконица
        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        notificationLayout.setTextViewText(R.id.notificationText, notificationText);
        notificationLayout.setImageViewBitmap(R.id.notifcationIcon, BitmapFactory.decodeResource(context.getResources(), R.drawable.tracker_large_icon));

        //Направи нотификацију
        final Notification notification = new NotificationCompat.Builder(context, "trackingchannel")
                .setCustomContentView(notificationLayout)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setSmallIcon(R.drawable.app_small_icon)
                .build();

        //Promenljiva za pracenje brzine kretanja korisnika
        lastLocation = null;

        sharedPreferences.edit().putString("LastSavedLocationName", "").apply();

        //Program pokrene LocationFinder koji pri svakom apdejtu lokacije obavestava korisnika
        finder = new LocationFinder(context);
        finder.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        finder.setInterval(30 * 1000);
        finder.setFastInterval(20 * 1000);
        finder.setOnUpdateEvent(new EventHandler() {
            @Override
            public void handle(Location location) {
                notifyUser(location);
                if (ServiceHandler.activityTest == 0 && finder.getPriority() != LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY) {
                    finder.stop();
                    finder.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    finder.start();
                }
                if (ServiceHandler.activityTest != 0 && finder.getPriority() == LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY) {
                    finder.stop();
                    finder.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    finder.start();
                }
            }
        });
        finder.start();

        /*Posto je android uveo odredjena pravila i ogranicenja za pozadinske servise koji rade van aplikacije
         * kada se servis pokrene, pokrene se i ona notifikacija koja ne moze da se skloni
        * */
        startForeground(4, notification);

        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyUser(Location location) {
        Log.i("Tracker", "----> Updated location!!");

        //Ako ne moze da dobije trenutnu lokaciju samo izadje iz funkcije
        if (location == null)
            return;

        //Ako se korisnik i dalje krece ili je servis tek poceo sa radom promeni poslednju zapamcenu lokaciju i izadje iz funkcije
        if (lastLocation == null){
            lastLocation = new SavedLocation("last", location);
            lastVisitedLocation = new SavedLocation("visited",location);
            return;
        }else if (lastLocation.distanceTo(location) > 60) {
            lastLocation = new SavedLocation("last", location);
            return;
        }
        
        lastLocation = new SavedLocation("last", location);

        List<SavedLocation> list = LocationSystem.loadLocations(getApplicationContext());
        SavedLocation nearestLocation = LocationSystem.findNearestLocation(list, location);

        //Ukoliko nema sacuvanih lokacija,nearestLocation je null pa ce izaci iz funkcije
        if (nearestLocation == null){
            return;
        }

        double dist = nearestLocation.distanceTo(location);
        System.out.println("[MRMI]: udaljenost:" + dist + ((finder.getPriority() == LocationRequest.PRIORITY_HIGH_ACCURACY) ? " HighAccuracy" : " BalancedBattery"));

        //Ukoliko je korisnik stao u blizini neke sacuvane lokacije salje se notifikacija sa podsetnikom
        if (dist < 75)
            sendAdequateNotification(nearestLocation);
        else {
            sendAdequateNotification(new SavedLocation("", location));
        }
    }

    //Пошаље одговарајућу нотификацију зависно од тренутне и претходне сачуване локације
    private void sendAdequateNotification(SavedLocation currentLocation) {
        String currentLocationName = currentLocation.getName();
        String lastSavedLocationName = sharedPreferences.getString("LastSavedLocationName", ""); //Пронађи последњу сачувану локацију на уређају
        NotificationSender notificationSender = new NotificationSender(Tracker.this);

        System.out.println("[MRMI]: Претходна: " + lastSavedLocationName + " Тренутна: " + currentLocationName);
        if ((currentLocationName.equals("") && (currentLocation.distanceTo(lastVisitedLocation) > 50 || lastVisitedLocation==null)) || (lastVisitedLocation.isHome()) && !currentLocation.isHome()) {
            notificationSender.showNotification(0); //Покажи нотификацију за стављање маске кад корисник изађе из куће
        }

        if (currentLocation.isHome() && !lastVisitedLocation.isHome()) {
            notificationSender.showNotification(1); //Покажи нотификацију за дезинфекцију одеће и маске кад се корисник врати кући
        }

        if (!lastVisitedLocation.isHome() && !currentLocation.isHome() && !lastSavedLocationName.equals(currentLocationName)) {
            notificationSender.showNotification(2); //Покажи нотификацију за дезинфекцију руку кад пређе из објекта у објекат
        }

        sharedPreferences.edit().putString("LastSavedLocationName", currentLocationName).apply(); //Промени име последње сачуване локације
        lastVisitedLocation = currentLocation;
    }

    @Override
    public void onDestroy() {
        //Pri prekidu servisa,gasi se kanal napravljen na pocetku i prekida se periodicno lociranje (linija 134)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.deleteNotificationChannel("trackingchannel");
        }
        sharedPreferences.edit().putBoolean("trackerRunning", false).apply();
        sharedPreferences.edit().putBoolean("trackerSwitch", false).apply();
        finder.stop();
        super.onDestroy();
    }
}
