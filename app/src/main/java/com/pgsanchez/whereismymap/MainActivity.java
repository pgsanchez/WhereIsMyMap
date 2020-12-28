package com.pgsanchez.whereismymap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    // Defines para mensajes entre Activities
    private static final int NEW_MAP_ACTIVITY = 101;

    DBMapRepositoryImpl dbMapRepository;

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
        if (requestCode == NEW_MAP_ACTIVITY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // TODO: Hay que guardar el mapa en la BD
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