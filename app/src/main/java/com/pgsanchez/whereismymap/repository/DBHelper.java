package com.pgsanchez.whereismymap.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MyMaps.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Crear la tabla "maps"
        sqLiteDatabase.execSQL("CREATE TABLE " + DBContract.MapEntry.TABLE_NAME + " ("
                + DBContract.MapEntry.ID_MAP + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DBContract.MapEntry.NAME + " TEXT,"
                + DBContract.MapEntry.LATITUDE + " REAL,"
                + DBContract.MapEntry.LONGITUDE + " REAL,"
                + DBContract.MapEntry.IMG_FILE_NAME + " TEXT,"
                + DBContract.MapEntry.CATEGORY + " TEXT,"
                + DBContract.MapEntry.DISTANCE + " TEXT,"
                + DBContract.MapEntry.MAP_DATE + " DATE,"
                + DBContract.MapEntry.RACE_DATE + " DATE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Actualización a versiones superiores
        /* if (oldVersion < 2)
        {
            upgrade_2(db); // Función que hará los cambios
        } */
    }
}
