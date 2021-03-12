package com.pgsanchez.whereismymap.presentation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.use_cases.UseCaseDB;

import java.util.ArrayList;
import java.util.List;

public class MapsListActivity extends AppCompatActivity {
    // Defines para mensajes entre Activities
    private static final int ACTIVITY_EDIT_MAP = 401;

    List mapList;
    private ItemMapAdapter adaptador;
    private RecyclerView recyclerView;
    // Caso de uso para obtener el listado de mapas correspondiente
    UseCaseDB useCaseDB;
    String textToFind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_list);

        useCaseDB = new UseCaseDB(this);

        mapList = new ArrayList<Map>();
        textToFind = (String)getIntent().getExtras().getString("name");
        if (textToFind.isEmpty()) {
            mapList = useCaseDB.getAllMaps();
        } else {
            mapList = useCaseDB.getMapsByName(textToFind);
        }
        adaptador = new ItemMapAdapter(mapList, ((Aplication) getApplication()).imgsPath);
        recyclerView = findViewById(R.id.mapsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptador);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mDividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.ic_line_divider_list));
        recyclerView.addItemDecoration(mDividerItemDecoration);



        Intent intent;
        intent = new Intent(this, EditMapActivity.class);

        adaptador.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Se obtiene la posición del elemento clicado (empezando por el 0)
                int pos = recyclerView.getChildAdapterPosition(v);
                Log.d("MapsList:onClick: ", "position " + Integer.toString(pos));
                Map map = (Map) mapList.get(pos);

                // Se llama a la ventana de edición, pasándole el mapa
                intent.putExtra("mapId", (long)-1);
                intent.putExtra("objMap", map);
                startActivityForResult(intent, ACTIVITY_EDIT_MAP);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_EDIT_MAP){
            if (resultCode == Activity.RESULT_OK){
                // recargar la lista, porque ha habido cambios
                mapList.clear();
                cargarLista();
            }
        }
    }

    void cargarLista(){
        if (textToFind.isEmpty()) {
            mapList = useCaseDB.getAllMaps();
        } else {
            mapList = useCaseDB.getMapsByName(textToFind);
        }
        adaptador.notifyDataSetChanged();
    }
}