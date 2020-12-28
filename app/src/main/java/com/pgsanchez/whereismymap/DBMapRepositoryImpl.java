package com.pgsanchez.whereismymap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBMapRepositoryImpl implements MapRepository{
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
                map.setDistance(c.getDouble(c.getColumnIndex(DBContract.MapEntry.DISTANCE)));
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
    public void addMap(Map mapa) {

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
        map.setDistance(c.getDouble(c.getColumnIndex(DBContract.MapEntry.DISTANCE)));
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
}
