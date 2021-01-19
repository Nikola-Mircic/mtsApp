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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.app.mtsapp.LanguageManager;
import com.app.mtsapp.MainActivity;
import com.app.mtsapp.NotificationSender;
import com.app.mtsapp.R;
import com.app.mtsapp.location.EventHandler;
import com.app.mtsapp.location.LocationFinder;
import com.app.mtsapp.location.LocationSystem;
import com.app.mtsapp.location.SavedLocation;
import com.google.android.gms.location.LocationRequest;

import java.util.List;

public class Tracker extends Service implements LocationListener {
    private boolean shouldStop = false;

    private LocationFinder finder;

    private SavedLocation lastLocation;

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
        Context context = this.getApplicationContext();//Ova linija je tu da malo ulepsa kod ( da ne pozivam stalno ovu funkciju)

        LanguageManager languageManager = new LanguageManager(this);
        languageManager.checkLocale();

        String text = getResources().getString(R.string.serviceNotificationText);

        Intent nIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, nIntent, 0);
        final Notification notification = new NotificationCompat.Builder(context, "trackingchannel")
                .setContentTitle("Coro-No")
                .setContentText(text)//Ovo pise pre nego sto se pronadje prva lokacija
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_six_feet)
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
        Log.i("Tracker", "----> Updated location!!");
        Context context = getApplicationContext();

        //Ako ne moze da dobije trenutnu lokaciju samo izadje iz funkcije
        if (location == null)
            return;

        //Ako se korisnik i dalje krece ili je servis tek poceo sa radom promeni poslednju zapamcenu lokaciju i izadje iz funkcije
        if (lastLocation == null){
            lastLocation = new SavedLocation("last", location);
            return;
        }else if (lastLocation.distanceTo(location) > 60) {
            lastLocation = new SavedLocation("last", location);
            return;
        }
        
        lastLocation = new SavedLocation("last", location);

        List<SavedLocation> list = LocationSystem.loadLocations(context);
        SavedLocation nearestLocation = LocationSystem.findNearestLocation(context, list, location);

        //Ukoliko nema sacuvanih lokacija,nearestLocation je null pa ce izaci iz funkcije
        if (nearestLocation == null)
            return;

        double dist = nearestLocation.distanceTo(location);
        System.out.println("[MRMI]: udaljenost:" + dist);

        //Ukoliko je korisnik stao u blizini neke sacuvane lokacije salje se notifikacija sa podsetnikom
        if (dist < 75)
            sendAdequateNotification(nearestLocation.getName());
        else {
            sendAdequateNotification("");
        }
    }

    //Пошаље одговарајућу нотификацију зависно од тренутне и претходне сачуване локације
    private void sendAdequateNotification(String currentLocationName) {
        String lastSavedLocationName = sharedPreferences.getString("LastSavedLocationName", ""); //Пронађи последњу сачувану локацију на уређају
        NotificationSender notificationSender = new NotificationSender(Tracker.this);

        System.out.println("[MRMI]: Претходна: " + lastSavedLocationName + " Тренутна: " + currentLocationName);
        if (currentLocationName.equals("") || (lastSavedLocationName.equals("home")) && !currentLocationName.equals("home")) {
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
            NotificationChannel channel = manager.getNotificationChannel("trackingchannel");
            channel.setImportance(NotificationManager.IMPORTANCE_NONE);
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
