package com.example.Explorer.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
@Database(entities = {LocationEntity.class, ExploredArea.class, UserEntity.class, VisitedLocationEntity.class, POIEntity.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {

    private static void insertTestData(SupportSQLiteDatabase database, double latitude, double longitude, String name) {
        long timestamp = System.currentTimeMillis();

        String sql = "INSERT INTO locations (latitude, longitude, name, timestamp) VALUES (?, ?, ?, ?)";
        database.execSQL(sql, new Object[]{
                latitude,
                longitude,
                name,
                timestamp
        });
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE users (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "username TEXT, " +
                            "lastUpdated INTEGER NOT NULL)"
            );
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `points_of_interest` (" +
                    "`id` TEXT NOT NULL, " +
                    "`latitude` REAL NOT NULL, " +
                    "`longitude` REAL NOT NULL, " +
                    "`name` TEXT, " +
                    "`description` TEXT, " +
                    "`category` TEXT, " +
                    "`imageUrl` TEXT, " +
                    "`isVisited` INTEGER NOT NULL DEFAULT 0, " +
                    "`visitTimestamp` INTEGER NOT NULL DEFAULT 0, " +
                    "`city` TEXT, " +
                    "PRIMARY KEY(`id`))");

            database.execSQL("CREATE TABLE IF NOT EXISTS `visited_locations` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`poiId` TEXT, " +
                    "`visitTimestamp` INTEGER NOT NULL, " +
                    "`userLatitude` REAL NOT NULL, " +
                    "`userLongitude` REAL NOT NULL, " +
                    "`steps` INTEGER NOT NULL DEFAULT 0)");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            insertTestData(database, 55.5308, 89.2003, "Sharypovo");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            insertTestData(database, 55.5308, 89.2003, "Sharypovo");
        }
    };


    public static AppDatabase getDatabase(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, "explorer.db")
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .build();
    }

    public abstract LocationDao locationDao();
    public abstract UserDao userDao();

    public abstract POIDao poiDao();

    public abstract VisitedLocationDao visitedLocationDao();
    public abstract ExploredAreaDao exploredAreaDao();
}