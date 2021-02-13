package com.pgsanchez.whereismymap.presentation;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.repository.DBHelper;
import com.pgsanchez.whereismymap.repository.DBMapRepositoryImpl;
import com.pgsanchez.whereismymap.repository.MapRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Aplication extends Application {
    List mapList;

    // Ruta donde se guardarán las imágenes de los mapas
    public File imgsPath = null;

    public MapRepository dbMapRepository;
    @Override
    public void onCreate() {
        super.onCreate();
        mapList = new ArrayList<Map>();

        // Se establece la ruta en la que se guardarán los mapas. De momento, en la tarjeta SD
        imgsPath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());

        // Se crea la BD y el objeto que la va a manejar.
        dbMapRepository = new DBMapRepositoryImpl(getApplicationContext());
    }

}
