package com.app.mtsapp.location.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.app.mtsapp.MainActivity;
import com.app.mtsapp.NotificationSender;
import com.app.mtsapp.R;
import com.app.mtsapp.location.LocationEventUpdate;
import com.app.mtsapp.location.LocationFinder;
import com.app.mtsapp.location.LocationSystem;
import com.app.mtsapp.location.SavedLocation;

import java.util.List;

public class Tracker extends Service implements LocationListener {
    private boolean shouldStop = false;

    /*Lokacija se moze traziti i preko location findera tako da
     * je ceo kod jednostavniji
     * */
    private LocationFinder finder;

    public static double lastDistance = -1.0;
    public static String locationName = "";

    //Референца SharedPreferences-a: лаког начина чувања простих података, овде због слања адекватне нотификације при промени локације
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);

        //Program kreira odvojeni kanal za notifikacije poslate sa servisa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("trackingchannel", "TrackChannel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = this.getApplicationContext();//Ova linija je tu da malo ulepsa kod ( da ne pozivam stalno ovu funkciju)

        Intent nIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, nIntent, 0);
        final Notification notification = new NotificationCompat.Builder(context, "trackingchannel")
                .setContentTitle("Coro-No Official app")
                .setContentText("Service is running...")//Ovo pise pre nego sto se pronadje prva lokacija
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_six_feet)
                .setNotificationSilent()
                .build();

        //Program pokrene LocationFinder koji pri svakom apdejtu lokacije obavestava korisnika
        finder = new LocationFinder(context);
        finder.setOnUpdateEvent(new LocationEventUpdate() {
            @Override
            public void onUpdate(Location location) {
                notifyUser(location);
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

    @Override
    public void onLocationChanged(Location location) {
        notifyUser(location);
    }

    private void notifyUser(Location location) {
        Context context = getApplicationContext();

        List<SavedLocation> list = LocationSystem.loadLocations(context);
        SavedLocation nearestLocation = LocationSystem.findNearestLocation(context, list, location);

        if (nearestLocation == null) {
            nearestLocation = new SavedLocation("Test", 0, 0, 0);
        }

        double dist = nearestLocation.distanceTo(finder.getCurrentLocation());
        System.out.println("[MRMI]: udaljenost:" + dist);
        if (dist < 500) {
            sendAdequateNotification(nearestLocation.getName());
        }


        Intent nIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, nIntent, 0);
        double latData = ((int) (location.getLatitude() * 10000)) / 10000.0;
        double longData = ((int) (location.getLongitude() * 10000)) / 10000.0;
        Notification notification = new NotificationCompat.Builder(context, "trackingchannel")
                .setContentTitle("Coro-No Official app")
                .setContentText("" + latData + ", " + longData + " [" + nearestLocation.getName() + "]")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_six_feet)
                .setNotificationSilent()
                .build();

        //Promeni notifikaciju (tekst notifikacije koji je na pocetku bi 'Test') u latitude i longitude lokacije
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(4, notification);
        }
    }

    //Пошаље одговарајућу нотификацију зависно од тренутне и претходне сачуване локације
    private void sendAdequateNotification(String currentLocationName) {
        String lastSavedLocationName = sharedPreferences.getString("LastSavedLocationName", ""); //Пронађи последњу сачувану локацију на уређају
        NotificationSender notificationSender = new NotificationSender(Tracker.this);

        System.out.println("[MRMI]: Претходна: " + lastSavedLocationName + " Тренутна: " + currentLocationName);
        if ((lastSavedLocationName.equals("") || lastSavedLocationName.equals("home")) && !currentLocationName.equals("home")) {
            notificationSender.showNotification(0); //Покажи нотификацију за стављање маске кад корисник изађе из куће
            sharedPreferences.edit().putString("LastSavedLocationName", currentLocationName).apply(); //Промени име последње сачуване локације
        }
        if (currentLocationName.equals("home") && !lastSavedLocationName.equals("home")) {
            notificationSender.showNotification(1); //Покажи нотификацију за дезинфекцију одеће и маске кад се корисник врати кући
            sharedPreferences.edit().putString("LastSavedLocationName", currentLocationName).apply(); //Промени име последње сачуване локације
        }
        if (!lastSavedLocationName.equals("home") && !currentLocationName.equals("home") && !lastSavedLocationName.equals(currentLocationName)) {
            notificationSender.showNotification(2); //Покажи нотификацију за дезинфекцију руку кад пређе из објекта у објекат
            sharedPreferences.edit().putString("LastSavedLocationName", currentLocationName).apply(); //Промени име последње сачуване локације
        }
    }

    @Override
    public void onDestroy() {
        //Pri prekidu servisa,gasi se kanal napravljen na pocetku i prekida se periodicno lociranje (linija 134)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.deleteNotificationChannel("trackingchannel");
        }
        finder.stop();
        super.onDestroy();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
