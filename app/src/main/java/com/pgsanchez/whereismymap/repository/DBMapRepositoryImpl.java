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

        String orderBy = DBContract.MapEntry.RACE_DATE + " DESC";

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
    public Map getMapById(long id) {
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
        String[] selectionArgs = {Long.toString(id)};
        Cursor c = db.query(
                DBContract.MapEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                DBContract.MapEntry.ID_MAP + "=?",   // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        // Solo debe haber un resultado
        if (c.moveToFirst()){
            map = cursorObjectToMapObject(c);
        }

        return map;
    }

    @Override
    public List<Map> getMapsByName(String name) {
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

        String orderBy = DBContract.MapEntry.RACE_DATE + " DESC";
        // Valor de la clausula WHERE (where id_map = id)
        String[] selectionArgs = {"%" + name.toUpperCase() + "%"};

        // Se puede probar con esto:
        // Cursor c = db.rawQuery("SELECT * FROM tbl1 WHERE TRIM(name) = '"+name.trim()+"'", null);
        //mDb.rawQuery("SELECT * WHERE UPPER(name) = '" + "%" + name + "%" + "'", null);



        Cursor c = db.query(
                DBContract.MapEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                DBContract.MapEntry.NAME.toUpperCase() + " LIKE ?",      // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
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
    public long insertMap(Map mapa) {
        // Gets the data repository in write mode
        SQLiteDatabase db = baseDatos.getWritableDatabase();

        // Funcion de insertar. Está función devuelve el id del elemento insertado o -1 si ha habido un error. Devolvemos ese mismo valor
        long newId = db.insert(DBContract.MapEntry.TABLE_NAME, null, toContentValues(mapa));
        return newId;
    }

    /**
     *
     * @param mapa
     * @return devuelve el número de líneas afectadas (debería ser siempre 1)
     */
    @Override
    public int updateMap(Map mapa) {
        // Gets the data repository in write mode
        SQLiteDatabase db = baseDatos.getWritableDatabase();

        String whereClause = DBContract.MapEntry.ID_MAP + "='" + mapa.getId() + "'";

        int rows = db.update(DBContract.MapEntry.TABLE_NAME, toContentValues(mapa),whereClause, null);

        return rows;
    }

    /**
     *
     * @param mapa
     * @return devuelve el número de líneas borradas (debería ser siempre 1)
     */
    @Override
    public int deleteMap(Map mapa) {
        // Gets the data repository in write mode
        SQLiteDatabase db = baseDatos.getWritableDatabase();

        String whereClause = DBContract.MapEntry.ID_MAP + "='" + mapa.getId() + "'";

        int rows = db.delete(DBContract.MapEntry.TABLE_NAME, whereClause, null);
        return rows;
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
        String mapDate = formatter2.format(map.getMapDate());
        values.put(DBContract.MapEntry.MAP_DATE, mapDate);

        return values;
    }
}
