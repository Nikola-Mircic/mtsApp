package com.app.mtsapp.location.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.app.mtsapp.NotificationSender;
import com.app.mtsapp.location.LocationFinder;
import com.app.mtsapp.location.LocationSystem;
import com.app.mtsapp.location.SavedLocation;

public class Tracker extends IntentService {
    public static volatile boolean shouldStop = false;

    public static double lastDistance = -1.0;
    public static String locationName = "";

    public Tracker() {
        super(Tracker.class.getSimpleName());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int sec = 1;
        int delay = 15000;
        LocationSystem tlsystem = new LocationSystem(ServiceHandler.lastActivityInstance);
        LocationFinder tfinder = new LocationFinder(ServiceHandler.lastActivityInstance);
        tfinder.start();
        while(true){
            Log.i("HandleIntent", "--> Seconds: "+(sec++)*15);
            tlsystem.loadLocations();

            if(tlsystem==null || tfinder==null) {
                if(tlsystem==null)
                    Log.i("Tracker", "doWork: LocationSystem is null");
                if(tfinder==null)
                    Log.i("Tracker", "doWork: LocationFinder is null");
                return;
            }

            Location current = tfinder.getCurrentLocation();
            if(current==null){
                NotificationSender test = new NotificationSender(tlsystem.getActivity());
                test.showNotification("Coro-No official app", "Cann't find current location");
                SystemClock.sleep(delay);
                continue;
            }

            SavedLocation sl = tlsystem.findNearestLocation(current);
            if(sl==null){
                sl = new SavedLocation("Test location",0 ,0, 0);
            }

            String tempName = sl.getName();
            double tempDistance = sl.distanceTo(current);
            if(lastDistance != -1.0){
                if(tempName.equals(locationName) && Math.abs(tempDistance-lastDistance)<100){
                    //UKOLIKO SE LOKACIJA NIJE PROMENILA ZA VISE OD OKO STO METARA ZA 10 MIN
                    //KORISNIK JE VEROVATNO STAO ILI USPORIO
                    NotificationSender test = new NotificationSender(tlsystem.getActivity());
                    test.showNotification("Coro-No official app", sl.getName());
                }
            }

            locationName = tempName;
            lastDistance = tempDistance;
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(tempName+" "+ Math.round(tempDistance*10)/10);
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            SystemClock.sleep(delay);
        }
    }
}
