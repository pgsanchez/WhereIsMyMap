package com.pgsanchez.whereismymap.presentation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.repository.DBHelper;
import com.pgsanchez.whereismymap.repository.MapRepository;
import com.pgsanchez.whereismymap.use_cases.UseCaseDB;

public class MainActivity extends AppCompatActivity {

    // Defines para mensajes entre Activities
    private static final int NEW_MAP_ACTIVITY = 101;
    private static final int EDIT_MAP_ACTIVITY = 102;
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
        // Comprobar a qué activity respondemos
        // Posibles respuestas:
        //      1. Nuevo mapa + resultado ok
        //      2. Nuevo mapa + resultado cancel
        long newMapId = -1;
        if (requestCode == NEW_MAP_ACTIVITY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Recoger el objeto NuevoMapa
                newMapId = (long) data.getExtras().getSerializable("newMapId");
                // TODO
                // Recoger aquí el objeto Mapa con el nuevo ID y mostrar la página de Edit

                //Iniciar actividad de edición para comprobar que los datos están bien
                /*Intent intent = new Intent(this, EditMapActivity.class);
                intent.putExtra("mapa", objMap);
                startActivityForResult(intent, EDIT_MAP_ACTIVITY);*/
                Log.d("MainActivity: ", "onActivityResult OK");
            } else if (resultCode == RESULT_CANCELED){
                // Entra por aquí si el Insert de la BD ha fallado
                Log.d("MainActivity: ", "onActivityResult CANCELED");
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

    /**
     * Función a la que se llama cuando se pulsa el botón de "Buscar Mapa"
     * @param view
     */
    public void onMapsList(View view){
        //Log.d("onMapsList", ": hasta aqui");
        EditText edtTextToFind = findViewById(R.id.edtTextToFind);
        Intent intent;
        intent = new Intent(this, MapsListActivity.class);
        intent.putExtra("name", edtTextToFind.getText().toString());
        startActivity(intent);

        /*if (!edtTextToFind.getText().toString().isEmpty()){

        } else{
            // Listar todos los mapas
        }*/
    }
}