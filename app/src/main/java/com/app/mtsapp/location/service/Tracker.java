package com.app.mtsapp.location.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

import com.app.mtsapp.MainActivity;
import com.app.mtsapp.NotificationSender;
import com.app.mtsapp.R;
import com.app.mtsapp.location.LocationEventUpdate;
import com.app.mtsapp.location.LocationFinder;
import com.app.mtsapp.location.LocationSystem;
import com.app.mtsapp.location.SavedLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.Permission;
import java.util.List;

public class Tracker extends Service implements LocationListener {
    private boolean shouldStop = false;

    /*Lokacija se moze traziti i preko location findera tako da
    * je ceo kod jednostavniji
    * */
    private LocationFinder finder;

    public static double lastDistance = -1.0;
    public static String locationName = "";

    @Override
    public void onCreate() {
        super.onCreate();
        //Program kreira odvojeni kanal za notifikacije poslate sa servisa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("trackingchannel", "TrackChannel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager =  getApplicationContext().getSystemService(NotificationManager.class);
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
        startForeground(1, notification);
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

    private void notifyUser(Location location){
        Context context = getApplicationContext();

        List<SavedLocation> list = LocationSystem.loadLocations(context);
        SavedLocation near = LocationSystem.findNearestLocation(context, list, location);

        if(near==null){
            near = new SavedLocation("Test", 0,0,0);
        }

        Intent nIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, nIntent, 0);
        double latData = ((int)(location.getLatitude()*10000))/10000.0;
        double longData = ((int)(location.getLongitude()*10000))/10000.0;
        Notification notification = new NotificationCompat.Builder(context, "trackingchannel")
                .setContentTitle("Coro-No Official app")
                .setContentText(""+latData+", "+longData+" ["+near.getName()+"]")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_six_feet)
                .build();

        //Promeni notifikaciju (tekst notifikacije koji je na pocetku bi 'Test') u latitude i longitude lokacije
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(1, notification);
        }
    }

    @Override
    public void onDestroy() {
        //Pri prekidu servisa,gasi se kanal napravljen na pocetku i prekidja se periodicno lociranje (linija 134)
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
