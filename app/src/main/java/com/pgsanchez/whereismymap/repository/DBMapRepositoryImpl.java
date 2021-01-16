package com.pgsanchez.whereismymap.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pgsanchez.whereismymap.domain.Map;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBMapRepositoryImpl implements MapRepository {
    DBHelper baseDatos;

    private ArrayList<Map> mapList;

    public DBMapRepositoryImpl(Context  context) {
        mapList = new ArrayList<Map>();
        baseDatos = new DBHelper(context);
    }

    @Override
    public List<Map> getAllMaps() {
        // Gets the data repository in write mode
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DBContract.MapEntry.ID_MAP,
                DBContract.MapEntry.NAME,
                DBContract.MapEntry.LATITUDE,
                DBContract.MapEntry.LONGITUDE,
                DBContract.MapEntry.RACE_DATE,
                DBContract.MapEntry.MAP_DATE,
                DBContract.MapEntry.DISTANCE,
                DBContract.MapEntry.CATEGORY,
                DBContract.MapEntry.IMG_FILE_NAME,
        };

        String orderBy = DBContract.MapEntry.RACE_DATE + " ASC";

        Cursor c = db.query(
                DBContract.MapEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                orderBy                                 // The sort order
        );

        mapList.clear();
        if (c.moveToFirst()) {
            do {
                Map map = new Map();
                map.setId(c.getInt(c.getColumnIndex(DBContract.MapEntry.ID_MAP)));
                map.setName(c.getString(c.getColumnIndex(DBContract.MapEntry.NAME)));
                map.setCategory(c.getString(c.getColumnIndex(DBContract.MapEntry.CATEGORY)));
                map.setDistance(c.getString(c.getColumnIndex(DBContract.MapEntry.DISTANCE)));
                map.setLatitude(c.getDouble(c.getColumnIndex(DBContract.MapEntry.LATITUDE)));
                map.setLongitude(c.getDouble(c.getColumnIndex(DBContract.MapEntry.LONGITUDE)));
                map.setImgFileName(c.getString(c.getColumnIndex(DBContract.MapEntry.IMG_FILE_NAME)));

                ParsePosition pos = new ParsePosition(0);
                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
                Date raceDate = simpledateformat.parse(c.getString(c.getColumnIndex(DBContract.MapEntry.RACE_DATE)), pos);
                map.setRaceDate(raceDate);

                pos.setIndex(0);
                Date mapDate = simpledateformat.parse(c.getString(c.getColumnIndex(DBContract.MapEntry.MAP_DATE)), pos);
                map.setMapDate(mapDate);

                mapList.add(map);

            } while(c.moveToNext());
        }

        return mapList;
    }

    @Override
    public Map getMapById(int id) {
        // objeto Map que vamos a devolver
        Map map = new Map();
        // Gets the data repository in write mode
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DBContract.MapEntry.ID_MAP,
                DBContract.MapEntry.NAME,
                DBContract.MapEntry.LATITUDE,
                DBContract.MapEntry.LONGITUDE,
                DBContract.MapEntry.RACE_DATE,
                DBContract.MapEntry.MAP_DATE,
                DBContract.MapEntry.DISTANCE,
                DBContract.MapEntry.CATEGORY,
                DBContract.MapEntry.IMG_FILE_NAME,
        };

        // Valor de la clausula WHERE (where id_map = id)
        String[] selectionArgs = {Integer.toString(id)};
        Cursor c = db.query(
                DBContract.MapEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                DBContract.MapEntry.ID_MAP,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        // Solo debe haber un resultado
        if (c.getCount() == 1){
            map = cursorObjectToMapObject(c);
        }

        return map;
    }

    @Override
    public List<Map> getMapsByName(String name) {
        return null;
    }

    @Override
    public int addMap(Map mapa) {
        // Gets the data repository in write mode
        SQLiteDatabase db = baseDatos.getWritableDatabase();

        // Funcion de insertar.
        if (db.insert(DBContract.MapEntry.TABLE_NAME, null, toContentValues(mapa)) == -1) {
            // Mostrar mensaje de error
            return -1;
        }

        return 0;
    }

    @Override
    public void updateMap(Map mapa) {

    }

    @Override
    public void deleteMap(Map mapa) {

    }

    @Override
    public void deleteMap(int id) {

    }

    Map cursorObjectToMapObject(Cursor c){
        Map map = new Map();
        map.setId(c.getInt(c.getColumnIndex(DBContract.MapEntry.ID_MAP)));
        map.setName(c.getString(c.getColumnIndex(DBContract.MapEntry.NAME)));
        map.setCategory(c.getString(c.getColumnIndex(DBContract.MapEntry.CATEGORY)));
        map.setDistance(c.getString(c.getColumnIndex(DBContract.MapEntry.DISTANCE)));
        map.setLatitude(c.getDouble(c.getColumnIndex(DBContract.MapEntry.LATITUDE)));
        map.setLongitude(c.getDouble(c.getColumnIndex(DBContract.MapEntry.LONGITUDE)));
        map.setImgFileName(c.getString(c.getColumnIndex(DBContract.MapEntry.IMG_FILE_NAME)));

        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
        Date raceDate = simpledateformat.parse(c.getString(c.getColumnIndex(DBContract.MapEntry.RACE_DATE)), pos);
        map.setRaceDate(raceDate);

        pos.setIndex(0);
        Date mapDate = simpledateformat.parse(c.getString(c.getColumnIndex(DBContract.MapEntry.MAP_DATE)), pos);
        map.setMapDate(mapDate);

        return map;
    }

    public ContentValues toContentValues(Map map)
    {
        ContentValues values = new ContentValues();
        values.put(DBContract.MapEntry.NAME, map.getName());
        values.put(DBContract.MapEntry.CATEGORY, map.getCategory());
        values.put(DBContract.MapEntry.DISTANCE, map.getDistance());
        values.put(DBContract.MapEntry.LATITUDE, map.getLatitude());
        values.put(DBContract.MapEntry.LONGITUDE, map.getLongitude());
        values.put(DBContract.MapEntry.IMG_FILE_NAME, map.getImgFileName());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String raceDate = formatter.format(map.getRaceDate());
        values.put(DBContract.MapEntry.RACE_DATE, raceDate);

        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
        String mapDate = formatter2.format(map.getRaceDate());
        values.put(DBContract.MapEntry.MAP_DATE, mapDate);

        return values;
    }
}
