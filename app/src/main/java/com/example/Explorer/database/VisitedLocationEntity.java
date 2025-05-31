package com.example.Explorer.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "visited_locations")
public class VisitedLocationEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String poiId;
    public long visitTimestamp;
    public double userLatitude;
    public double userLongitude;
    public int steps;
}