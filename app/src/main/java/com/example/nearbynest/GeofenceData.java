package com.example.nearbynest;

public class GeofenceData {
    protected String id;
    protected String lat;
    protected String lon;

    public GeofenceData(String id, String lat, String lon){
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }
}
