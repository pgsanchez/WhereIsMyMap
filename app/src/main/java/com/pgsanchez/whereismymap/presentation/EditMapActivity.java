package com.pgsanchez.whereismymap.presentation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EditMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Creamos un objeto Mapa que es el que vamos a mostrar en la pantalla
    Map mapa = new Map();
    // Ruta donde se guardarán las imágenes de los mapas
    File imgsPath = null;
    // Spinners para seleccionar la categoría y la distancia del nuevo mapa
    private Spinner categories;
    private Spinner distance;
    // Mapa de Google Maps
    private GoogleMap gMap;
    // Caso de uso para guardar el mapa en la BD
    UseCaseDB useCaseDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        long idMap = (long)getIntent().getExtras().getSerializable("mapId");

        // Se inicializa el mapa
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        // Se establece la ruta en la que se guardarán los mapas. De momento, en la tarjeta SD
        imgsPath = ((Aplication) getApplication()).imgsPath;
        //imgsPath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());
        useCaseDB = new UseCaseDB(this);
        mapa = useCaseDB.getMapById(idMap);

        iniciarDatos();
        habilitarEdicion(false);
    }

    // Funcion para poner los datos por defecto en la pantalla
    public void iniciarDatos(){
        // Nombre
        TextView name  = (TextView) findViewById(R.id.edtName);
        name.setText(mapa.getName());

        // Foto del mapa
        ImageView foto = findViewById(R.id.imageViewMap);
        if (mapa.getImgFileName() != null && !mapa.getImgFileName().isEmpty()) {
            foto.setImageURI(Uri.parse(imgsPath + "/" + mapa.getImgFileName()));
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
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
    }

    public void habilitarEdicion(boolean habilitar){
        TextView name  = findViewById(R.id.edtName);
        TextView raceDate  = findViewById(R.id.edtRaceDate);
        TextView mapDate  = findViewById(R.id.edtMapDate);
        ImageView imgViewDelete = findViewById(R.id.imgViewDelete);
        ImageView iconRaceDateCalendar = findViewById(R.id.iconRaceDateCalendar);
        ImageView iconMapDateCalendar = findViewById(R.id.iconMapDateCalendar);

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
    }
}