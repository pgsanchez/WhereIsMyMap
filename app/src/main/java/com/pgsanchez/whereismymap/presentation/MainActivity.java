package com.pgsanchez.whereismymap.presentation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.repository.DBMapRepositoryImpl;
import com.pgsanchez.whereismymap.repository.MapRepository;

public class MainActivity extends AppCompatActivity {

    // Defines para mensajes entre Activities
    private static final int NEW_MAP_ACTIVITY = 101;

    MapRepository dbMapRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Se crea la BD y el objeto que la va a manejar.
        dbMapRepository = new DBMapRepositoryImpl(getApplicationContext());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Comprobar a qué activity respondemos
        // Posibles respuestas:
        //      1. Nuevo mapa + resultado ok
        //      2. Nuevo mapa + resultado cancel
        Map objMap = new Map();
        if (requestCode == NEW_MAP_ACTIVITY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // TODO: Hay que guardar el mapa en la BD
                // Recoger el objeto NuevoMapa
                objMap = (Map) data.getExtras().getSerializable("parametro");
                // Guardarlo en la BD
                //dbMapRepository.addMap(objMap);
            }
        }

    }

    /**
     * Función a la que se llama cuando se pulsa el botón "Nuevo Mapa".
     * Llamará al formulario de crear nuevo mapa
     * @param view
     */
    public void onNewMap(View view) {
        Intent intent;

        intent = new Intent(this, NewMapActivity.class);
        startActivityForResult(intent, NEW_MAP_ACTIVITY);
    }
}