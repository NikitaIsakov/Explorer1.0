package com.example.Explorer.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "explored_areas")
public class ExploredArea {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public double north;
    @NonNull
    public double south;
    @NonNull
    public double east;
    @NonNull
    public double west;
    @NonNull
    public long timestamp;
}