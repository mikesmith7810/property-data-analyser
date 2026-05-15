package com.mike.db;

public class NearestStationData {
    public String name;
    public double distance;
    public String unit;

    public NearestStationData() {}

    public NearestStationData(String name, double distance, String unit) {
        this.name = name;
        this.distance = distance;
        this.unit = unit;
    }
}
