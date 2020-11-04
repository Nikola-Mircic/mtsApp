package com.app.mtsapp.location;

import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

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
        A:for(File data : dir.listFiles()){
            try {
                SavedLocation sl;
                FileInputStream fis = activity.openFileInput(data.getName());
                ObjectInputStream ois = new ObjectInputStream(fis);
                sl = (SavedLocation) ois.readObject();
                ois.close();
                for(SavedLocation test : locations){
                    if(test.getName().equals(sl.getName()))
                        continue A;
                }
                locations.add(sl);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public List<SavedLocation> getLocations(){
        return this.locations;
    }
}
