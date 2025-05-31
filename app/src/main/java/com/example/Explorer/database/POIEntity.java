package com.example.Explorer.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "points_of_interest")
public class POIEntity {
    @PrimaryKey
    @NonNull
    public String id;

    public double latitude;
    public double longitude;
    public String name;
    public String description;
    public String category; // museum, park, monument, etc.
    public String imageUrl;
    public boolean isVisited = false;
    public long visitTimestamp = 0;
    public String city;
}