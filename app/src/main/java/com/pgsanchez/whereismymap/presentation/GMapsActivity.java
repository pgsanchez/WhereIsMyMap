package com.pgsanchez.whereismymap.presentation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.use_cases.UseCaseDB;

import java.util.ArrayList;
import java.util.List;

public class GMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Caso de uso para actualizar/borrar el mapa en la BD
    UseCaseDB useCaseDB;
    List mapList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_g_maps);

        useCaseDB = new UseCaseDB(this);
        mapList = new ArrayList<Map>();
        mapList = useCaseDB.getAllMaps();

        // Se inicializa el mapa
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Situarse en Madrid
        LatLng madrid = new LatLng(40.41, -3.60);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 9));
        for (int i=0; i<mapList.size(); i++) {
            Map mapa = (Map) mapList.get(i);
            LatLng point = new LatLng(mapa.getLatitude(), mapa.getLongitude());
            mMap.addMarker(new MarkerOptions().position(point)
                    .title(mapa.getName())
                    .snippet("Mapa " + Integer.toString(mapa.getId())));
        }

        Intent intent;
        intent = new Intent(this, EditMapActivity.class);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                // Se obtiene el ID del mapa a partir del "snippet"
                String snippet = marker.getSnippet(); // Ej. "Mapa 123"
                Long mapId = Long.parseLong(snippet.substring(5));
                Map mapSelected = null;
                for (int i=0; i<mapList.size(); i++){
                    Map mapaDeLista = (Map) mapList.get(i);
                    if (mapaDeLista.getId() == mapId){
                        mapSelected = mapaDeLista;
                        break;
                    }
                }

                if (mapSelected != null) {
                    intent.putExtra("mapId", (long) -1);
                    intent.putExtra("objMap", mapSelected);
                    startActivity(intent);
                }
            }
        });
    }

}