package com.pgsanchez.whereismymap.presentation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.pgsanchez.whereismymap.R;

import java.io.File;

public class MapView extends AppCompatActivity {
    // Ruta donde se guardarán las imágenes de los mapas (es global)
    File imgsPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        // Se establece la ruta en la que se guardarán los mapas. De momento, en la tarjeta SD
        imgsPath = ((Aplication) getApplication()).imgsPath;
        String mapName = (String)getIntent().getExtras().getSerializable("mapName");
        //File imgMapFile = imgsPath + "/" + imgMapPath;
        File mapFile = new File(imgsPath + "/" + mapName);
        PhotoView photoView = (PhotoView) findViewById(R.id.img_map_view);
        //photoView.setImageResource(imgMapPath);
        Glide.with(this).load(mapFile).into(photoView);
    }
}