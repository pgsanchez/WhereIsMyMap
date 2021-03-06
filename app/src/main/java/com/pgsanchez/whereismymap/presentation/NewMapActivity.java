package com.pgsanchez.whereismymap.presentation;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.CategoryName;
import com.pgsanchez.whereismymap.domain.DistanceType;
import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.use_cases.UseCaseDB;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
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

import java.io.File;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NewMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Defines para mensajes entre Activities
    private static final int ACTIVITY_CAMERA = 201;
    private static final int PERMISSIONS_REQUEST = 202;

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
        Toolbar toolbar = findViewById(R.id.toolbarEditNewMap);
        setSupportActionBar(toolbar);

        useCaseDB = new UseCaseDB(this);

        // Se inicializa el mapa
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        // Se añaden dos escuchadores para los iconos de cambiar las fechas de la carrera y del mapa
        findViewById(R.id.iconRaceDateCalendar).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) { changeRaceDate(); } });
        findViewById(R.id.iconMapDateCalendar).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) { changeMapDate(); } });

        // Se establece la ruta en la que se guardarán los mapas. De momento, en la tarjeta SD
        imgsPath = ((Aplication) getApplication()).imgsPath;

        iniciarDatos();
        habilitarVisibilidad(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.save_option:
                onGuardar();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
        // - y que aparezca seleccionada la primera distancia
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

                // Hay que mostrar esta imagen en la vista.
                if (uriUltimaFoto.getLastPathSegment() != null){
                    newMap.setImgFileName(uriUltimaFoto.getLastPathSegment());
                    //foto.setImageURI(Uri.parse(imgsPath + "/" + newMap.getImgFileName()));
                    //File tmpFile = new File(imgsPath + "/" + newMap.getImgFileName());
                    Glide.with(this)
                            .load(uriUltimaFoto)
                            .override(120, 120)
                            .into(foto);

                } else{
                    // asignamos el nombre de la imagen (cadena vacía) al objeto mapa
                    newMap.setImgFileName("");
                    foto.setImageBitmap(null);
                }
                habilitarVisibilidad(false);
            } else {
                Toast.makeText(this, "Error en captura" + uriUltimaFoto.toString(), Toast.LENGTH_LONG).show();
                /* Cuando se cancela la foto, como el archivo donde se va a guardar lo hemos creado
                antes en uriUltimaFoto, dicho archivo se guarda en la carpeta de mapas, pero estará vacío.
                Hay que elminarlo aquí y poner uriUltimaFoto a null.*/
                DeleteImageMapFromPath(uriUltimaFoto.getLastPathSegment());
                // Se ponen las variables de la imagen a NULL
                newMap.setImgFileName("");
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
        String[] PERMISSIONS = {
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (Build.VERSION.SDK_INT >= 23) {
            if (!hasAllPermissions(PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST);
            } else {
                takePhoto();
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("permisos", "Permission is granted");
        }
    }

    public boolean hasAllPermissions(String[] permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 1){
                    if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED))
                        // Permission is granted. Continue the action or workflow
                        // in your app.
                        Log.i("permissionsResult", "All permissions granted");
                } else {
                    Log.i("permissionsResult", "NOT All permissions granted");
                }
                break;
            default:
                break;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }


    private void takePhoto(){
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

        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int selectedyear, int selectedmonth, int selectedday) {
                c.set(selectedyear, selectedmonth, selectedday);
                newMap.setRaceDate(new Date(c.getTimeInMillis()));
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                TextView raceDate  = (TextView) findViewById(R.id.edtRaceDate);
                raceDate.setText(dateFormat.format(newMap.getRaceDate()));
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

        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int selectedyear, int selectedmonth, int selectedday) {
                c.set(selectedyear, selectedmonth, selectedday);
                newMap.setMapDate(new Date(c.getTimeInMillis()));
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                TextView mapDate  = (TextView) findViewById(R.id.edtMapDate);
                mapDate.setText(dateFormat.format(newMap.getMapDate()));
            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle("Fecha del Mapa");
        mDatePicker.show();
    }

    public void onGuardar()
    {
        exitCanceling = false;
        /* Antes de nada hay que hacer una comprobación de que existen los datos obligatorios
        y, si no, no se permite la operación de guardar.
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

        long newMapId = useCaseDB.insertMap(newMap);

        Intent intent;
        intent = new Intent(this, EditMapActivity.class);
        if (newMapId != -1) {
            intent.putExtra("mapId", newMapId);
            startActivity(intent);
        } else {
            setResult(RESULT_CANCELED, intent);
        }
        finish();
    }

    // Funcionalidad del botón de Borrar la imagen
    public void DeleteMapImgage(View view){
        if (DeleteImageMapFromPath(newMap.getImgFileName())) {
            uriUltimaFoto = null;
            foto.setImageBitmap(null);
            newMap.setImgFileName("");
        }
        habilitarVisibilidad(true);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng madrid = new LatLng(40.41, -3.60);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 9));

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
            if(!newMap.getImgFileName().isEmpty()) {
                DeleteImageMapFromPath(newMap.getImgFileName());
            }
        }

    }

    public void habilitarVisibilidad(boolean habilitar){

        /* El botón de hacer la foto y el de borrarla son contrarios para la visibilidad: cuando
        uno es visible, el otro no. El true/false que se pasa hace referencia al botón de hacer
        la foto. El otro botón es opuesto.
         */
        ImageButton imgBtnPhoto = findViewById(R.id.imgBtnPhoto);
        ImageView imgViewDelete = findViewById(R.id.imgViewDelete);
        if (habilitar) {
            imgBtnPhoto.setVisibility(View.VISIBLE);
            imgViewDelete.setVisibility(View.INVISIBLE);

        } else{
            imgBtnPhoto.setVisibility(View.INVISIBLE);
            imgViewDelete.setVisibility(View.VISIBLE);
        }
    }
}