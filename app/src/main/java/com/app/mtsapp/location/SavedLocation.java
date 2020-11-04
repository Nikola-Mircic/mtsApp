package com.app.mtsapp.location;

import android.location.Location;

import java.io.Serializable;
import java.security.Provider;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SavedLocation implements Serializable{
    private static int id = 1;
    private SimpleDateFormat formater = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy");

    private String name;
    private Date lastDate;
    private double altitude,latitude,longitude;

    public SavedLocation() {
        this("#" + id, new Date(),0, 0, 0);
    }

    public SavedLocation(Location location) {
        this("#" + id, new Date(), location.getAltitude(), location.getLatitude(), location.getLongitude());
    }

    public SavedLocation(String name,Location location){
        this(name, new Date(), location.getAltitude(), location.getLatitude(), location.getLongitude());
    }

    public SavedLocation(String name,Date lastDate,Location location){
        this(name, lastDate, location.getAltitude(), location.getLatitude(), location.getLongitude());
    }

    public SavedLocation(String name, Date lastDate, double altitude, double latitude, double longitude){
        ++this.id;
        this.setName(name);
        this.setLastDate(lastDate);
        this.setAltitude(altitude);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public Date getLastDate(){
        return this.lastDate;
    }

    public void setLastDate(Date lastDate){
        this.lastDate = lastDate;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
