package com.app.mtsapp.location;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.app.mtsapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocationSystem {
    private Activity activity;
    private List<SavedLocation> locations;
    private List<List<SavedLocation>> distance;

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
                FileOutputStream fos = activity.openFileOutput(filename, Context.MODE_PRIVATE);
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
            return;
        }

        A:
        for (File data : Objects.requireNonNull(dir.listFiles())) {
            try {
                SavedLocation sl;
                FileInputStream fis = activity.openFileInput(data.getName());
                ObjectInputStream ois = new ObjectInputStream(fis);
                sl = (SavedLocation) ois.readObject();
                ois.close();
                for (int i=0;i<locations.size();++i) {
                    if (locations.get(i).getName().equals(sl.getName())) {
                        locations.set(i,sl);
                        continue A;
                    }
                }
                locations.add(sl);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static List<SavedLocation> loadLocations(Context context){
        Log.i("[Load location] ", "getting locations... ");
        List<SavedLocation> list = new ArrayList<>();

        File dir = new File(context.getFilesDir(), "saved_loc");
        if(!dir.exists())
            return list;

        for (File data : Objects.requireNonNull(dir.listFiles())) {
            try {
                SavedLocation sl;
                FileInputStream fis = context.openFileInput(data.getName());
                ObjectInputStream ois = new ObjectInputStream(fis);
                sl = (SavedLocation) ois.readObject();
                ois.close();
                list.add(sl);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return list;
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
                Toast.makeText(activity, activity.getResources().getString(R.string.locationDeletedToast), Toast.LENGTH_SHORT).show();
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

    public static SavedLocation findNearestLocation(Context context, List<SavedLocation> locations, Location current){
        double minDist = -1;
        SavedLocation near = null;

        for(SavedLocation sl : locations){
            if(sl.distanceTo(current)<minDist || minDist == -1){
                minDist = sl.distanceTo(current);
                near = sl;
            }
        }

        return near;
    }
}

