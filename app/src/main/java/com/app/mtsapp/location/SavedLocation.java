package com.app.mtsapp.location;

import android.location.Location;

import java.io.Serializable;

public class SavedLocation implements Serializable{
    private static int id = 1;

    private String name;
    private double altitude,latitude,longitude;

    public SavedLocation() {
        this("#" + id, 0, 0, 0);
    }

    public SavedLocation(Location location) {
        this("#" + id, location.getAltitude(), location.getLatitude(), location.getLongitude());
    }

    public SavedLocation(String name,Location location){
        this(name, location.getAltitude(), location.getLatitude(), location.getLongitude());
    }

    public SavedLocation(String name, double altitude, double latitude, double longitude) {
        ++this.id;
        this.setName(name);
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
