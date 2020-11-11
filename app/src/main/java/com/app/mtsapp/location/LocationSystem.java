package com.app.mtsapp.location;

import android.app.Activity;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class LocationSystem {
    private Activity activity;
    private List<SavedLocation> locations;

    public LocationSystem(Activity activity){
        this.locations = new ArrayList<>();
        this.activity = activity;
    }

    public void addLocation(String name,Location location){
        SavedLocation temp = new SavedLocation(name,location);
        locations.add(temp);
    }

    public void addLocation(SavedLocation sl){
        for(SavedLocation temp : locations) {
            if (sl.getName().equals(temp.getName())) {
                //if(sl.getName().equals(sl.getName())) {
                Toast.makeText(activity, "Location already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        locations.add(sl);
    }

    public void saveLocations(){
        File dir = new File(activity.getFilesDir(), "saved_loc");
        if(!dir.exists()){
            dir.mkdir();
        }

        for(SavedLocation location : locations){
            try {
                String filename = "location_"+location.getName();
                File file = new File(dir,filename);
                Log.i("File path", ""+file.getAbsolutePath());
                FileOutputStream fos = activity.openFileOutput(filename,activity.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(location);
                file.createNewFile();
                oos.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void loadLocations(){
        Log.i("[Load location] ", "getting locations... ");
        File dir = new File(activity.getFilesDir(), "saved_loc");
        if(!dir.exists()){
            Toast.makeText(activity, "Dir not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(activity, "Found: "+dir.listFiles().length, Toast.LENGTH_SHORT).show();
        A:
        for (File data : Objects.requireNonNull(dir.listFiles())) {
            try {
                SavedLocation sl;
                FileInputStream fis = activity.openFileInput(data.getName());
                ObjectInputStream ois = new ObjectInputStream(fis);
                sl = (SavedLocation) ois.readObject();
                ois.close();
                for (SavedLocation test : locations) {
                    System.out.println("[МРМИ]: Тест име " + test + " сачувана локација име " + sl);
                    if (test.getName().equals(sl.getName()))
                        continue A;
                }
                locations.add(sl);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        if(locations.size()>1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                locations.sort(new Comparator<SavedLocation>() {
                    @Override
                    public int compare(SavedLocation o1, SavedLocation o2) {
                        return (o1.getLastDate().before(o2.getLastDate()) ? -1 : 1);
                    }
                });
            }
        }
    }

    public void removeLocation(String locationName){
        Log.i("[Remove location] ", "deleting locationn from list... ");
        SavedLocation temp = null;
        for(SavedLocation sl : locations){
            if(sl.getName().equals(locationName)) {
                temp = sl;
                break;
            }
        }
        if(temp!=null) {
            locations.remove(temp);
        }

        Log.i("[Remove location] ", "deleting location from memory... ");
        File dir = new File(activity.getFilesDir(), "saved_loc");
        if(!dir.exists())
            return;
        for(File tempFile : dir.listFiles()){
            if(tempFile.getName().equals("location_"+locationName)){
                boolean deleted = false;
                do{
                    try {
                        deleted = tempFile.delete();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }while(!deleted);
                Toast.makeText(activity, "Location deleted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public List<SavedLocation> getLocations(){
        return this.locations;
    }

    public SavedLocation getLocation(String name){
        for(SavedLocation sl : locations){
            if(sl.getName().equals(name))
                return sl;
        }
        return null;
    }

    public SavedLocation findNearestLocation(Location current){
        double dLat,dLong,dAlt;
        double minDist = -1;
        SavedLocation near = null;

        for(SavedLocation sl : locations){
            dLat = sl.getLatitude()-current.getLatitude();
            dLong = sl.getLongitude()-current.getLongitude();
            dAlt = sl.getAltitude()-current.getAltitude();
            if(dist(dLat, dLong, dAlt)<minDist || minDist == -1){
                minDist = dist(dLat, dLong, dAlt);
                near = sl;
            }
        }

        return near;
    }

    private double dist(double dLat, double dLong, double dAlt){
        return Math.sqrt(dLat*dLat + dLong*dLong + dAlt*dAlt);
    }
}
