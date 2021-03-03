package com.pgsanchez.whereismymap.presentation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.CategoryName;
import com.pgsanchez.whereismymap.domain.DistanceType;
import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.use_cases.UseCaseDB;

import java.io.File;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Defines para mensajes entre Activities
    private static final int ACTIVITY_CAMERA = 301;

    // Creamos un objeto Mapa que es el que vamos a mostrar en la pantalla
    Map mapa = new Map();
    // Ruta donde se guardarán las imágenes de los mapas (es global)
    File imgsPath = null;
    // Nombre de la imagen original (por si se cambia)
    String originalImage;
    // URI de la imagen que se ha capturado con la cámara
    Uri uriUltimaFoto = null;
    // Imagen del mapa que se va a mostrar en la ventana
    ImageView foto;

    // Spinners para seleccionar la categoría y la distancia del nuevo mapa
    private Spinner categories;
    private Spinner distance;
    // Mapa de Google Maps
    private GoogleMap gMap;
    // Caso de uso para actualizar/borrar el mapa en la BD
    UseCaseDB useCaseDB;

    //Booleano que indica si estamos en el modo de Edición
    boolean editMode = false;
    // exitCanceling: true si salimos de esta Activity con un CANCEL
    boolean exitCanceling = true;
    // mapsDeleted: valdrá 1 si se borra el mapa.
    int mapsDeleted = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_map);
        Toolbar toolbar = findViewById(R.id.toolbarEditNewMap);
        setSupportActionBar(toolbar);

        // Se establece la ruta en la que se guardarán los mapas. De momento, en la tarjeta SD
        imgsPath = ((Aplication) getApplication()).imgsPath;
        // Caso de uso para leer/actualizar/borrar el mapa en la BD
        useCaseDB = new UseCaseDB(this);

        // Se añaden dos escuchadores para los iconos de cambiar las fechas de la carrera y del mapa
        findViewById(R.id.iconRaceDateCalendar).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) { changeRaceDate(); } });
        findViewById(R.id.iconMapDateCalendar).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) { changeMapDate(); } });

        /* A esta ventana se llega por 2 caminos:
        - Desde la ventana de NewMapActivity: en este caso, en el parámetro mapId llegará el id del nuevo mapa,
        que será un valor > -1. Además, en este caso, en el parámetro "mapa" llegará un objeto vacío, así que
        no lo recogemos.
        - Desde la lista de mapas: En este caso, en el parámetro mapId llegará un valor -1 y en el parámetro
        "mapa" llegará el objeto mapa que hay que mostrar.
         */
        long idMap = (long)getIntent().getExtras().getSerializable("mapId");
        if (idMap > -1){
            // Venimos desde la ventana de NewMapActivity
            mapa = useCaseDB.getMapById(idMap);
        } else{
            // Venimos de la lista de mapas
            mapa = (Map)getIntent().getExtras().getSerializable("objMap");
        }

        originalImage = mapa.getImgFileName();

        iniciarDatos();
        habilitarVisibilidad(false);
        habilitarEdicion(false);

        // Se inicializa el mapa
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);

        MenuItem itemSave = menu.findItem(R.id.save_option);
        itemSave.setVisible(editMode);
        MenuItem itemDelete = menu.findItem(R.id.delete_option);
        itemDelete.setVisible(editMode);

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.edit_option:
                editMode = true;
                this.invalidateOptionsMenu();
                habilitarVisibilidad(true);
                habilitarEdicion(true);
                break;
            case R.id.save_option:
                onGuardarCambios();
                editMode = false;
                this.invalidateOptionsMenu();
                habilitarVisibilidad(false);
                habilitarEdicion(false);
                break;
            case R.id.delete_option:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Borrar Mapa");
                builder.setMessage("¿Quieres borrar el mapa?");

                builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mapsDeleted = borrarMapa();
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                AlertDialog dialog = builder.create();
                dialog.show();


                    editMode = false;
                    this.invalidateOptionsMenu();
                    habilitarVisibilidad(false);
                    habilitarEdicion(false);


                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_CAMERA) {
            if (resultCode == Activity.RESULT_OK && uriUltimaFoto!=null) {
                // al volver aquí, en uriUltimaFoto, tenemos la última foto tomada por la cámara y
                // con el nombre que le hemos puesto al hacerla (wimg_1234.jpg)

                // Hay que mostrar esta imagen en la vista.
                if (uriUltimaFoto.getLastPathSegment() != null){
                    // asignamos el nombre de la imagen (sin la ruta) al objeto mapa
                    mapa.setImgFileName(uriUltimaFoto.getLastPathSegment());
                    foto.setImageURI(Uri.parse(imgsPath + "/" + mapa.getImgFileName()));
                } else{
                        // asignamos el nombre de la imagen (cadena vacía) al objeto mapa
                    mapa.setImgFileName("");
                    foto.setImageBitmap(null);
                }

            } else {
                Toast.makeText(this, "Error en captura" + uriUltimaFoto.toString(), Toast.LENGTH_LONG).show();
                /* Cuando se cancela la foto, como el archivo donde se va a guardar lo hemos creado
                antes en uriUltimaFoto, dicho archivo se guarda en la carpeta de mapas, pero estará vacío.
                Hay que elminarlo aquí y poner uriUltimaFoto a null.*/

                DeleteImageMapFromPath(uriUltimaFoto.getLastPathSegment());
                // Se ponen las variables de la imagen a NULL
                mapa.setImgFileName("");
                uriUltimaFoto = null;
                foto.setImageBitmap(null);
            }
        }
    }

    // Funcion para poner los datos por defecto en la pantalla
    public void iniciarDatos(){
        // Id
        Toolbar toolbar = findViewById(R.id.toolbarEditNewMap);
        getSupportActionBar().setTitle("Mapa " + Integer.toString(mapa.getId()));
        // Nombre
        TextView name  = (TextView) findViewById(R.id.edtName);
        name.setText(mapa.getName());

        // Foto del mapa
        foto = findViewById(R.id.imageViewMap);
        if (mapa.getImgFileName() != null && !mapa.getImgFileName().isEmpty()) {
            foto.setImageURI(Uri.parse(imgsPath + "/" + mapa.getImgFileName()));
            // Y se oculta el botón de hacer fotos
        } else {
            foto.setImageBitmap(null);
        }

        // Spinner de "distancia"
        distance = findViewById(R.id.spnDistance);
        ArrayAdapter<String> adaptadorDist = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, DistanceType.getDistances());
        adaptadorDist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distance.setAdapter(adaptadorDist);
        for (int i=0; i < DistanceType.getDistances().length; i++){
            if (DistanceType.getDistances()[i].equals(mapa.getDistance())){
                distance.setSelection(i);
                break;
            }
        }

        // Spinner de "categoría"
        categories = findViewById(R.id.spnCategory);
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, CategoryName.getCategories());
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.setAdapter(adaptador);
        // y que aparezca seleccionaa la primera categoría
        for (int i=0; i < CategoryName.getCategories().length; i++){
            if (CategoryName.getCategories()[i].equals(mapa.getCategory())) {
                categories.setSelection(i);
                break;
            }
        }

        // Fecha de carrera
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        TextView raceDate  = (TextView) findViewById(R.id.edtRaceDate);
        raceDate.setText(dateFormat.format(mapa.getRaceDate()));

        // Fecha del mapa
        TextView mapDate  = (TextView) findViewById(R.id.edtMapDate);
        mapDate.setText(dateFormat.format(mapa.getMapDate()));


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Inicializar el mapa en la posición que viene en el objeto "mapa"
        gMap = googleMap;
        LatLng point = new LatLng(mapa.getLatitude(), mapa.getLongitude());
        gMap.addMarker(new MarkerOptions().position(point));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10));

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (editMode) {
                    gMap.clear();
                    gMap.addMarker(new MarkerOptions().position(point));

                    mapa.setLatitude(point.latitude);
                    mapa.setLongitude(point.longitude);
                }
            }
        });
    }

    public void habilitarEdicion(boolean habilitar){
        TextView name  = findViewById(R.id.edtName);
        TextView raceDate  = findViewById(R.id.edtRaceDate);
        TextView mapDate  = findViewById(R.id.edtMapDate);
        ImageView imgViewDelete = findViewById(R.id.imgViewDelete);
        ImageView iconRaceDateCalendar = findViewById(R.id.iconRaceDateCalendar);
        ImageView iconMapDateCalendar = findViewById(R.id.iconMapDateCalendar);
        ImageButton imgBtnPhoto = findViewById(R.id.imgBtnPhoto);

        // El mapa se tiene que poder "utilizar" (mover, zoom). Lo que hay que controlar con esta
        // función es que haga caso, o no, a las pulsaciones para cambiar la posición

        name.setEnabled(habilitar);
        distance.setEnabled(habilitar);
        categories.setEnabled(habilitar);
        raceDate.setEnabled(habilitar);
        mapDate.setEnabled(habilitar);
        imgViewDelete.setEnabled(habilitar);
        iconRaceDateCalendar.setEnabled(habilitar);
        iconMapDateCalendar.setEnabled(habilitar);
        imgBtnPhoto.setEnabled(habilitar);
    }

    public void habilitarVisibilidad(boolean habilitar){

        ImageView imgViewDelete = findViewById(R.id.imgViewDelete);
        if (habilitar) {
            imgViewDelete.setVisibility(View.VISIBLE);
        } else{
            imgViewDelete.setVisibility(View.INVISIBLE);
        }

        /*ImageButton imgBtnPhoto = findViewById(R.id.imgBtnPhoto);
        if (mapa.getImgFileName() != null && !mapa.getImgFileName().isEmpty()) {
            imgBtnPhoto.setVisibility(View.INVISIBLE);
        } else {
            imgBtnPhoto.setVisibility(View.VISIBLE);
        }*/
    }

    // Funcionalidad del botón de Borrar la imagen
    public void DeleteMapImgage(View view){
        if (!mapa.getImgFileName().equals(originalImage)){
            DeleteImageMapFromPath(mapa.getImgFileName());
        }
        uriUltimaFoto = null;
        foto.setImageBitmap(null);
        mapa.setImgFileName("");
    }

    /**
     * Elimina de la carpeta de mapas (imgsPath), la imagen que se le pasa por parámetro
     * @return TRUE en caso de que la imagen haya sido borrada. FALSE en caso contrario.
     */
    private boolean DeleteImageMapFromPath(String imgFileName){
        boolean deleted = false;
        File file = new File(imgsPath + "/" + imgFileName);
        deleted = file.delete();
        return deleted;
    }

    // 2- Funcionalidad de hacer nueva foto
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

    // 3- Funcionalidad de los "combos" de seleccionar las fechas
    // 4- Funcionalidad de Guardar los datos y actualizar la BD
    public void onGuardarCambios(){
        exitCanceling = false;

        // Nombre (no puede ser nulo)
        EditText edtName = (EditText) findViewById(R.id.edtName);
        if ((edtName.getText() == null) || edtName.getText().toString().equals("")){
            // Mostrar mensaje de error y salir
            Toast.makeText(this, "Introduzca nombre", Toast.LENGTH_SHORT).show();
            return;
        } else {
            mapa.setName(edtName.getText().toString());
        }

        mapa.setCategory(categories.getSelectedItem().toString());
        mapa.setDistance(distance.getSelectedItem().toString());

        // Fecha de carrera (podría ser nulo)
        EditText edtRaceDate = (EditText) findViewById(R.id.edtRaceDate);
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("dd/MM/yyyy");
        Date raceDate = simpledateformat.parse(edtRaceDate.getText().toString(), pos);
        mapa.setRaceDate(raceDate);

        // Fecha del mapa
        pos.setIndex(0);
        EditText edtMapDate = (EditText) findViewById(R.id.edtMapDate);
        Date mapDate = simpledateformat.parse(edtMapDate.getText().toString(), pos);
        mapa.setMapDate(mapDate);

        // Posición (no puede ser nulo)
        if((mapa.getLatitude() == 0.0) && (mapa.getLongitude() == 0.0)){
            Toast.makeText(this, "Introduzca localización", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar la BD
        useCaseDB.updateMap(mapa);
        // Borrar imagen original (solo si ha cambiado)
        /*if(!mapa.getImgFileName().isEmpty()) {
            Toast.makeText(this, "Nombre de mapa no vacío. Hay que borrar imagen original", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(this, "Nombre de mapa VACIO", Toast.LENGTH_SHORT).show();
        }

        if(!mapa.getImgFileName().equals(originalImage)) {
            Toast.makeText(this, "Nombre de mapa distino del original. Hay que borrar imagen original", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(this, "Nombre de mapa igual al original", Toast.LENGTH_SHORT).show();
        }*/

        if(!mapa.getImgFileName().isEmpty() || (!mapa.getImgFileName().equals(originalImage))){
            DeleteImageMapFromPath(originalImage);
            Toast.makeText(this, "Imagen borrada", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        /* Esta función se ejecuta al terminar la actividad, da igual si con un ok o si con un cancel*/
        /* Hay que comprobar si estamos terminando con un cancel y, en ese caso, borrar la imagen que
        hemos guardado en la carpeta
         */
        if (exitCanceling){
            if(!mapa.getImgFileName().isEmpty() && (!mapa.getImgFileName().equals(originalImage))) {
                DeleteImageMapFromPath(mapa.getImgFileName());
                Log.d("NewMapActivity:OnStop", "  OnStop");
            }
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
                mapa.setRaceDate(new Date(c.getTimeInMillis()));
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                TextView raceDate  = (TextView) findViewById(R.id.edtRaceDate);
                raceDate.setText(dateFormat.format(mapa.getRaceDate()));
                Log.d("onDataSet", dateFormat.format(mapa.getRaceDate()));
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
                mapa.setMapDate(new Date(c.getTimeInMillis()));
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                TextView mapDate  = (TextView) findViewById(R.id.edtMapDate);
                mapDate.setText(dateFormat.format(mapa.getMapDate()));
                Log.d("onDataSet", dateFormat.format(mapa.getMapDate()));
            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle("Fecha del Mapa");
        mDatePicker.show();
    }

    private int borrarMapa(){
        Log.d("EditMapActivity: ", "borrarMapa()");
        String imgFileName = mapa.getImgFileName();
        mapsDeleted = useCaseDB.deteleMap(mapa);

        if (mapsDeleted > 0) {
            if (!DeleteImageMapFromPath(imgFileName)){
                Toast.makeText(this, "Mapa borrado: LA IMAGEN NO SE HA PODIDO BORRAR", Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(this, "Mapa borrado - Imagen borrada", Toast.LENGTH_LONG).show();
            }
            Intent data = new Intent();
            setResult(RESULT_OK, data);
            finish();
        } else {
            Toast.makeText(this, "No se ha podido borrar el mapa", Toast.LENGTH_LONG).show();
        }

        return mapsDeleted;
    }
}