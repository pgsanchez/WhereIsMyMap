package com.pgsanchez.whereismymap.presentation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.repository.DBHelper;
import com.pgsanchez.whereismymap.repository.MapRepository;
import com.pgsanchez.whereismymap.use_cases.UseCaseDB;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    UseCaseDB useCaseDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        useCaseDB = new UseCaseDB(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* Comprobar a qué activity respondemos
        *  Posibles respuestas:
        *      1. Nuevo mapa + resultado ok
        *      2. Nuevo mapa + resultado cancel
        */
    }

    /**
     * Función a la que se llama cuando se pulsa el botón "Nuevo Mapa".
     * Llamará al formulario de crear nuevo mapa
     * @param view
     */
    public void onNewMap(View view) {
        Intent intent;
        intent = new Intent(this, NewMapActivity.class);
        startActivity(intent);
    }

    /**
     * Función a la que se llama cuando se pulsa el botón de "Buscar Mapa"
     * @param view
     */
    public void onMapsList(View view){
        EditText edtTextToFind = findViewById(R.id.edtTextToFind);
        // Se llama a la ventana del listado de mapas con el texto que se desea buscar. Puede ser null.
        Intent intent;
        intent = new Intent(this, MapsListActivity.class);
        intent.putExtra("name", edtTextToFind.getText().toString());
        startActivity(intent);
    }

    public void onBtnNumFiles(View view) {
        TextView tvNumFiles = findViewById(R.id.tvNumFiles);
        //Defino la ruta donde busco los ficheros
        File f = ((Aplication) getApplication()).imgsPath;
        //Creo el array de tipo File con el contenido de la carpeta
        File[] files = f.listFiles();

        tvNumFiles.setText(Integer.toString(files.length));
    }

    public void onBtnGMap(View view) {
        // Mostrar activity de google maps
        Intent intent;
        intent = new Intent(this, GMapsActivity.class);
        startActivity(intent);
    }
}