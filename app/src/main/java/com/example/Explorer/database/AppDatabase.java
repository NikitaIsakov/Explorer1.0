package com.example.Explorer.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
@Database(entities = {LocationEntity.class, ExploredArea.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static void insertTestData(SupportSQLiteDatabase database, double lat, double lon, String description) {
        double radius = 0.005;
        long timestamp = System.currentTimeMillis();

        String sql = "INSERT INTO explored_areas (north, south, east, west, timestamp) VALUES (?, ?, ?, ?, ?)";
        database.execSQL(sql, new Object[]{
                lat + radius,  // north
                lat - radius,  // south
                lon + radius,  // east
                lon - radius,  // west
                timestamp
        });
    }

    public static AppDatabase getDatabase(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, "explorer.db")
                .fallbackToDestructiveMigration()
                .build();
    }

    public abstract LocationDao locationDao();
    public abstract ExploredAreaDao exploredAreaDao();
}