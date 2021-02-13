package com.pgsanchez.whereismymap.presentation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.use_cases.UseCaseDB;

import java.util.ArrayList;
import java.util.List;

public class MapsListActivity extends AppCompatActivity {
    List mapList;
    private ItemMapAdapter adaptador;
    private RecyclerView recyclerView;
    // Caso de uso para obtener el listado de mapas correspondiente
    UseCaseDB useCaseDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_list);

        useCaseDB = new UseCaseDB(this);

        mapList = new ArrayList<Map>();
        String textToFind = (String)getIntent().getExtras().getString("name");
        if (textToFind.isEmpty()) {
            mapList = useCaseDB.getAllMaps();
        } else {
            mapList = useCaseDB.getMapsByName(textToFind);
        }
        Log.d("mapList size = ", Integer.toString(mapList.size()));

        //adaptador = ((Aplication) getApplication()).adaptador;
        adaptador = new ItemMapAdapter(mapList, ((Aplication) getApplication()).imgsPath);
        recyclerView = findViewById(R.id.mapsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptador);

        adaptador.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //int itemPosition = recyclerView.getChildLayoutPosition(v);
                // Se obtiene la posición del elemento clicado (empezando por el 0)
                int pos = recyclerView.getChildAdapterPosition(v);
                Map map = (Map) mapList.get(pos);
                // int pos = (Integer)(v.getTag());
                //Log.d("itemPosition: ", Integer.toString(itemPosition));
                Log.d("pos: ", Integer.toString(pos));
                Log.d("mapa: ", map.getName());

                Y aquí habría que llamar a la ventana de edición, pasándole el mapa
            }
        });
    }
}