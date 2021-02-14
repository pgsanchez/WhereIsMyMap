package com.pgsanchez.whereismymap.presentation;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.CategoryName;
import com.pgsanchez.whereismymap.domain.DistanceType;
import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.use_cases.UseCaseDB;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NewMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Defines para mensajes entre Activities
    private static final int ACTIVITY_CAMERA = 201;
    private static final int ACTIVITY_GALLERY = 202;

    // Spinners para seleccionar la categoría y la distancia del nuevo mapa
    private Spinner categories;
    private Spinner distance;
    // Ruta donde se guardarán las imágenes de los mapas
    File imgsPath = null;
    // Imagen del mapa que se va a mostrar en la ventana
    ImageView foto;
    // URI de la imagen que se ha capturado con la cámara
    Uri uriUltimaFoto = null;

    // Variable que guardará los datos que se introduzcan en los campos de esta ventana
    Map newMap = new Map();

    // Mapa de Google Maps
    private GoogleMap mMap;

    // Caso de uso para guardar el mapa en la BD
    UseCaseDB useCaseDB;

    // exitCanceling: true si salimos de esta Activity con un CANCEL
    boolean exitCanceling = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        useCaseDB = new UseCaseDB(this);

        // El botón flotante será el de Guardar los datos
        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                // Se recogen los datos y se guardan en un objeto mapa
                // Y, a continuación, se insertan en la BD
                onGuardar();
            }
        });

        // Se inicializa el mapa
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        // Se añaden dos escuchadores para los iconos de cambiar las fechas de la carrera y del mapa
        findViewById(R.id.iconRaceDateCalendar).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) { changeRaceDate(); } });
        findViewById(R.id.iconMapDateCalendar).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) { changeMapDate(); } });

        // Se establece la ruta en la que se guardarán los mapas. De momento, en la tarjeta SD
        imgsPath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());


        iniciarDatos();
    }

    // Funcion para poner los datos por defecto en la pantalla
    public void iniciarDatos(){
        // Aquí hay que iniciar varias cosas:
        // 1 - que en el desplegable de categorías aparezca la lista de categorias
        categories = findViewById(R.id.spnCategory);
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, CategoryName.getCategories());
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.setAdapter(adaptador);
        // y que aparezca seleccionaa la primera categoría
        categories.setSelection(0);

        // 2 - que en el desplegable de distancias aparezca la lista de distancias
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

        // Se inicializa el objeto que muestra la foto
        foto = findViewById(R.id.imageViewMap);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_CAMERA) {
            if (resultCode == Activity.RESULT_OK && uriUltimaFoto!=null) {
                // al volver aquí, en uriUltimaFoto, tenemos la última foto tomada por la cámara y
                // con el nombre que le hemos puesto al hacerla (wimg_1234.jpg)

                // asignamos el nombre de la imagen (sin la ruta) al objeto newMap
                newMap.setImgFileName(uriUltimaFoto.getLastPathSegment());
                // Hay que mostrar esta imagen en la vista.
                if (newMap.getImgFileName() != null && !newMap.getImgFileName().isEmpty()) {
                    foto.setImageURI(Uri.parse(imgsPath + "/" + newMap.getImgFileName()));
                } else {
                    foto.setImageBitmap(null);
                }
            } else {
                Toast.makeText(this, "Error en captura" + uriUltimaFoto.toString(), Toast.LENGTH_LONG).show();
                /* Cuando se cancela la foto, como el archivo donde se va a guardar lo hemos creado antes en uriUltimaFoto,
                dicho archivo se guarda en la carpeta de mapas, pero estará vacío. Hay que elminarlo aquí y poner
                uriUltimaFoto a null.*/
                // asignamos el nombre de la imagen (sin la ruta) al objeto newMap. Si no, no lo podremos borrar.
                newMap.setImgFileName(uriUltimaFoto.getLastPathSegment());
                DeleteImageMapFromPath();
                // Se ponen las variables de la imagen a NULL
                newMap.setImgFileName(null);
                uriUltimaFoto = null;
                foto.setImageBitmap(null);
            }
        }
    }


    /**
     * Se llama a esta función desde el bótón de la "cámara" de la actividad.
     * Se crea un fichero para guardar la imagen que se va a tomar con la cámara.
     * El fichero de la imagen se guardará en la ruta imgsPath con el nombre wimg_xxx.jpg,
     * donde xxx es una cadena de números que no se repite nunca.
     * @param view
     */
    public void onImgBtnPhoto(View view){
       try {
            File file = File.createTempFile(
                    "wimg_" + (System.currentTimeMillis()/ 1000), ".jpg" ,
                    imgsPath);
            if (Build.VERSION.SDK_INT >= 24) {
                uriUltimaFoto = FileProvider.getUriForFile(
                        this, "com.pgsanchez.whereismymap.fileProvider", file);
            } else {
                uriUltimaFoto = Uri.fromFile(file);
            }
            Intent intent   = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra (MediaStore.EXTRA_OUTPUT, uriUltimaFoto);
            startActivityForResult(intent, ACTIVITY_CAMERA);
        } catch (IOException ex) {
            Toast.makeText(this, "Error al crear fichero de imagen",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * La función changeRaceDate es la encargada de mostrar el diálogo para cambiar la fecha de
     * la carrera, recoger la nueva fecha y mostrarla en el editText de dicha fecha.
     */
    private void changeRaceDate(){
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        Log.d("changeRaceDate():", "dentro.");

        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int selectedyear, int selectedmonth, int selectedday) {
                //txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                Log.d("changeRaceDate():", "onDataSet.");

                c.set(selectedyear, selectedmonth, selectedday);
                newMap.setRaceDate(new Date(c.getTimeInMillis()));
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                TextView raceDate  = (TextView) findViewById(R.id.edtRaceDate);
                raceDate.setText(dateFormat.format(newMap.getRaceDate()));
                Log.d("onDataSet", dateFormat.format(newMap.getRaceDate()));
            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle("Fecha de carrera");
        mDatePicker.show();
    }

    /**
     * La función changeMapDate es la encargada de mostrar el diálogo para cambiar la fecha del
     * mapa, recoger la nueva fecha y mostrarla en el editText de dicha fecha.
     */
    private void changeMapDate(){
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        Log.d("changeMapDate():", "dentro.");

        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int selectedyear, int selectedmonth, int selectedday) {
                Log.d("changeRaceDate():", "onDataSet.");
                c.set(selectedyear, selectedmonth, selectedday);
                newMap.setMapDate(new Date(c.getTimeInMillis()));
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                TextView mapDate  = (TextView) findViewById(R.id.edtMapDate);
                mapDate.setText(dateFormat.format(newMap.getMapDate()));
                Log.d("onDataSet", dateFormat.format(newMap.getMapDate()));
            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle("Fecha del Mapa");
        mDatePicker.show();
    }

    public void onGuardar()
    {
        /* Antes de nada hay que hacer una comprobación de que existen los datos obligatorios
        y, si no, no se permite la operación de guardar.
        Tiene que existir el nombre
        Tiene que haber foto
         */

        // Nombre (no puede ser nulo)
        EditText edtName = (EditText) findViewById(R.id.edtName);
        if ((edtName.getText() == null) || edtName.getText().toString().equals("")){
            // Mostrar mensaje de error y salir
            Toast.makeText(this, "Introduzca nombre", Toast.LENGTH_SHORT).show();
            return;
        } else {
            newMap.setName(edtName.getText().toString());
        }

        newMap.setCategory(categories.getSelectedItem().toString());
        newMap.setDistance(distance.getSelectedItem().toString());

        // Fecha de carrera (podría ser nulo)
        EditText edtRaceDate = (EditText) findViewById(R.id.edtRaceDate);
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("dd/MM/yyyy");
        Date raceDate = simpledateformat.parse(edtRaceDate.getText().toString(), pos);
        newMap.setRaceDate(raceDate);

        // Fecha del mapa
        pos.setIndex(0);
        EditText edtMapDate = (EditText) findViewById(R.id.edtMapDate);
        Date mapDate = simpledateformat.parse(edtMapDate.getText().toString(), pos);
        newMap.setMapDate(mapDate);

        // Posición (no puede ser nulo)
        if((newMap.getLatitude() == 0.0) && (newMap.getLongitude() == 0.0)){
            Toast.makeText(this, "Introduzca localización", Toast.LENGTH_SHORT).show();
            return;
        }

        /*
        Si la imagen es de la galería, hay que guardarla en la carpeta de las imágenes.
        A continuación hay que guardar el nuevo mapa en la BD
         */

        long newMapId = useCaseDB.insertMap(newMap);
        exitCanceling = false;

        // Cerrar la Activity y salir con OK y devolviendo el id del nuevo mapa
        Intent data = new Intent();
        if (newMapId != -1) {
            /*data.putExtra("newMapId", newMapId);
            setResult(RESULT_OK, data);*/
            Intent intent;
            intent = new Intent(this, EditMapActivity.class);
            intent.putExtra("mapId", newMapId);
            startActivity(intent);
        } else {
            setResult(RESULT_CANCELED, data);
        }
        finish();
    }

    public void DeleteMapImgage(View view){
        if (DeleteImageMapFromPath()){
            uriUltimaFoto = null;
            foto.setImageBitmap(null);
        }
    }

    /**
     * Elimina de la carpeta de mapas (imgsPath), la imagen que se encuentra guardada en el objeto
     * newMap (newMap.getImgFileName())
     * @return TRUE en caso de que la imagen haya sido borrada. FALSE en caso contrario.
     */
    private boolean DeleteImageMapFromPath(){
        boolean deleted = false;
        if (newMap.getImgFileName() != null && !newMap.getImgFileName().isEmpty()){
            File file = new File(imgsPath + "/" + newMap.getImgFileName());

            deleted = file.delete();

            if (deleted) {
                //Toast.makeText(getBaseContext(), "Imagen borrada", Toast.LENGTH_LONG).show();
                newMap.setImgFileName(null);
            }
        }
        return deleted;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point));

                newMap.setLatitude(point.latitude);
                newMap.setLongitude(point.longitude);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        /* Esta función se ejecuta al terminar la actividad, da igual si con un ok o si con un cancel*/
        /* Lo que habría que hacer es comprobar, de alguna manera, si estamos terminando con un cancel y,
        en ese caso, borrar la imagen que hemos guardado en la carpeta
         */
        if (exitCanceling){
            if(DeleteImageMapFromPath()) {
                Log.d("NewMapActivity:OnStop", "  OnStop");
            }
        }

    }
}