package com.example.Explorer.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ExploredAreaDao {
    @Insert
    void insert(ExploredArea area);

    @Query("SELECT * FROM explored_areas")
    List<ExploredArea> getAllExploredAreas();

    @Query("DELETE FROM explored_areas WHERE id = :id")
    void deleteById(int id);
}