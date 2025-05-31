package com.example.Explorer.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VisitedLocationDao {
    @Insert
    void insert(VisitedLocationEntity visitedLocation);

    @Query("SELECT * FROM visited_locations ORDER BY visitTimestamp DESC")
    List<VisitedLocationEntity> getAllVisitedLocations();

    @Query("SELECT * FROM visited_locations WHERE poiId = :poiId ORDER BY visitTimestamp DESC LIMIT 1")
    VisitedLocationEntity getLastVisitForPOI(String poiId);

    @Query("SELECT COUNT(*) FROM visited_locations")
    int getTotalVisitsCount();

    @Query("SELECT SUM(steps) FROM visited_locations WHERE steps > 0")
    int getTotalStepsFromVisits();

    @Query("DELETE FROM visited_locations WHERE poiId = :poiId")
    void deleteVisitsForPOI(String poiId);
}