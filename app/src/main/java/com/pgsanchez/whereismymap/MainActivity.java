package com.pgsanchez.whereismymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Función a la que se llama cuando se pulsa el botón "Nuevo Mapa".
     * Llamará al formulario de crear nuevo mapa
     * @param view
     */
    public void onNewMap(View view) {
        Intent intent;

        intent = new Intent(this, NewMapActivity.class);
        startActivityForResult(intent, 101);
    }
}