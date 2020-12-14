package com.pgsanchez.whereismymap;

import android.provider.BaseColumns;

public final class DBContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DBContract() {}

    /* Inner class that defines the table contents */
    public static abstract class MapEntry implements BaseColumns {
        // Nombre de la tabla
        public static final String TABLE_NAME = "maps";
        // Campos
        public static final String ID_MAP =     "id_map";
        public static final String NAME =       "name";
        public static final String LATITUDE =   "latitude";
        public static final String LONGITUDE =  "longitude";
        public static final String IMG_FILE_NAME = "img_file_name";
        public static final String CATEGORY =   "category";
        public static final String DISTANCE =   "distance";
        public static final String MAP_DATE =   "map_date";
        public static final String RACE_DATE =  "race_date";
    }
}
