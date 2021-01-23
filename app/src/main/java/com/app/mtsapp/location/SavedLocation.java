package com.app.mtsapp.location;

import android.location.Location;

import java.io.Serializable;

public class SavedLocation implements Serializable{
    private String name;
    private double altitude,latitude,longitude;

    private boolean home;

    public SavedLocation(String name,Location location){
        this(name, location.getAltitude(), location.getLatitude(), location.getLongitude());
    }

    public SavedLocation(String name, double altitude, double latitude, double longitude) {
        this.setName(name);
        this.setAltitude(altitude);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public double distanceTo(Location location){
        if (location == null) {
            return -1.0;
        }

        Location temp = new Location("temp");
        temp.setLatitude(this.latitude);
        temp.setLongitude(this.longitude);
        temp.setAltitude(this.altitude);

        return temp.distanceTo(location);
    }

    public double distanceTo(SavedLocation location) {
        if (location == null) {
            return -1.0;
        }

        Location temp = new Location("temp");
        temp.setLatitude(this.latitude);
        temp.setLongitude(this.longitude);
        temp.setAltitude(this.altitude);

        Location temp2 = new Location("temp2");
        temp2.setLatitude(location.latitude);
        temp2.setLongitude(location.longitude);
        temp2.setAltitude(location.altitude);

        return temp.distanceTo(temp2);
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

    public boolean isHome() {
        return home;
    }

    public SavedLocation setHome(boolean home) {
        this.home = home;
        return this;
    }
}
