package com.pgsanchez.whereismymap.presentation;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.CategoryName;
import com.pgsanchez.whereismymap.domain.DistanceType;
import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.repository.DBMapRepositoryImpl;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewMapActivity extends AppCompatActivity {
    // Defines para mensajes entre Activities
    private static final int ACTIVITY_CAMERA = 201;

    private Spinner categories;
    private Spinner distance;

    Map newMap = new Map(); // Variable que guardará los datos que se introduzcan en los campos de esta ventana
    Uri uriUltimaFoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                DBMapRepositoryImpl dbMapRepository = new DBMapRepositoryImpl(getApplicationContext());
                // Se recogen los datos y se guardan en un objeto mapa
                // Y, a continuación, se insertan en la BD
            }
        });

        iniciarDatos();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_CAMERA) {
            if (resultCode == Activity.RESULT_OK && uriUltimaFoto!=null) {
                newMap.setImgFileName(uriUltimaFoto.toString());
                // Hay que mostrar esta imagen en la vista.
                //usoLugar.ponerFoto(pos, lugar.getFoto(), foto);
            } else {
                Toast.makeText(this, "Error en captura", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Funcion para poner los datos por defecto en la pantalla
    public void iniciarDatos(){
        // Aquí hay que iniciar varias cosas:
        // - que en el desplegable de categorías aparezca la lista de categorias
        categories = findViewById(R.id.spnCategory);
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, CategoryName.getCategories());
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.setAdapter(adaptador);
        // y que aparezca seleccionaa la primera categoría
        categories.setSelection(0);

        // - que en el desplegable de distancias aparezca la lista de distancias
        distance = findViewById(R.id.spnDistance);
        ArrayAdapter<String> adaptadorDist = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, DistanceType.getDistances());
        adaptadorDist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distance.setAdapter(adaptadorDist);
        // - y que aparezca seleccionada la primera distancia ("--")
        distance.setSelection(0);

        // - que en la fecha de carrera aparezca la fecha actual
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        TextView raceDate  = (TextView) findViewById(R.id.edtRaceDate);
        raceDate.setText(dateFormat.format(date));
        // - que en la fecha del mapa aparezca vacía

    }

    public void onImgBtnPhoto(){
        /*
        1. Sacar la cámara y hacer la foto
        2. Guardar imagen en GoogleDrive
        3. Visualizar la imagen en la ventana
        4. Guardar el nombre de la imagen en el objeto Map para que luego se guarde en la BD
         */
        try {
            File file = File.createTempFile(
                    "wimg_" + (System.currentTimeMillis()/ 1000), ".jpg" ,
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            if (Build.VERSION.SDK_INT >= 24) {
                uriUltimaFoto = FileProvider.getUriForFile(
                        this, "com.pgsanchez.whereismymap.fileProvider", file);
            } else {
                uriUltimaFoto = Uri.fromFile(file);
            }
            Intent intent   = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra (MediaStore.EXTRA_OUTPUT, uriUltimaFoto);
            startActivityForResult(intent, ACTIVITY_CAMERA);
            // Aquí, en teoría, en uriUltimaFoto está la imagen tomada.
            //return uriUltimaFoto;
        } catch (IOException ex) {
            Toast.makeText(this, "Error al crear fichero de imagen",
                    Toast.LENGTH_LONG).show();
            //return null;
        }

    }



    public void onGuardar(View view)
    {
        /* Antes de nada hay que hacer una comprobación de que existen los datos obligatorios
        y, si no, no se permite la operación de guardar.
        Tiene que existir el nombre
        Tiene que haber foto
         */
        // Nombre (no puede ser nulo)
        EditText edtName = (EditText) findViewById(R.id.edtName);

        // Categoría va a ser un spinner (podría ser nulo)

        // Distancia va a ser un spinner (podría ser nulo)


        // Fecha de carrera (podría ser nulo)
        EditText edtRaceDate = (EditText) findViewById(R.id.edtRaceDate);
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("dd/MM/yyyy");
        Date raceDate = simpledateformat.parse(edtRaceDate.getText().toString(), pos);

        // Fecha del mapa
        pos.setIndex(0);
        EditText edtMapDate = (EditText) findViewById(R.id.edtMapDate);
        Date mapDate = simpledateformat.parse(edtMapDate.getText().toString(), pos);

/*
        dlgRepostaje.setFecha(mydate);

        EditText edtImporte = (EditText) findViewById(R.id.edtImporte);
        dlgRepostaje.setImporte(Float.parseFloat(edtImporte.getText().toString()));
*/

    }

}