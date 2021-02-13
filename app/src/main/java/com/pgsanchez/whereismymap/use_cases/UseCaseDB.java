package com.pgsanchez.whereismymap.use_cases;

import android.app.Activity;

import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.presentation.Aplication;

import java.util.List;

public class UseCaseDB {
    private Activity actividad;

    public UseCaseDB(Activity actividad){
        this.actividad = actividad;
    }

    public int insertMap(Map newMap){
        int i = ((Aplication) actividad.getApplication()).dbMapRepository.insertMap(newMap);
        return i;
    }

    public List<Map> getAllMaps(){
        return ((Aplication) actividad.getApplication()).dbMapRepository.getAllMaps();
    }

    public List<Map> getMapsByName(String name){
        return ((Aplication) actividad.getApplication()).dbMapRepository.getMapsByName(name);
    }
}
