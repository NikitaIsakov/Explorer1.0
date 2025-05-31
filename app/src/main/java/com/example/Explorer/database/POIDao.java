package com.example.Explorer.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface POIDao {
    @Query("SELECT * FROM points_of_interest WHERE city = :city")
    List<POIEntity> getPOIsByCity(String city);

    @Query("SELECT * FROM points_of_interest WHERE isVisited = 1 ORDER BY visitTimestamp DESC LIMIT 1")
    POIEntity getLastVisitedPOI();

    @Query("SELECT COUNT(*) FROM points_of_interest WHERE city = :city")
    int getTotalPOIsInCity(String city);

    @Query("SELECT COUNT(*) FROM points_of_interest WHERE city = :city AND isVisited = 1")
    int getVisitedPOIsInCity(String city);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPOI(POIEntity poi);

    @Update
    void updatePOI(POIEntity poi);
}