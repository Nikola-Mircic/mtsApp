package com.app.mtsapp.location;

import android.location.Location;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SavedLocation implements Serializable{
    private static int id = 0;
    private SimpleDateFormat formater = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy");

    private String name;
    private Date lastDate;
    private Location location;

    public SavedLocation() {
        ++this.id;
        this.name = "#" + id;
        this.lastDate = new Date();
        this.location = null;
    }

    public SavedLocation(String name,Location location){
        this.name = name;
        this.lastDate = new Date();
        this.location = location;
    }

    public SavedLocation(String name,String lastDate,Location location){
        this.name = name;
        try {
            this.lastDate = formater.parse(lastDate);
        } catch (ParseException e) {
            this.lastDate = new Date();
            e.printStackTrace();
        }
        this.location = location;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getLastDate(){
        return this.lastDate.toString();
    }

    public void setLastDate(String lastDate){
        try {
            this.lastDate = formater.parse(lastDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
