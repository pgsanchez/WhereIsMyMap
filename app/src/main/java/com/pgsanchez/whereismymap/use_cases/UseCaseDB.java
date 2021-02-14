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

    public long insertMap(Map newMap){
        long mapId = ((Aplication) actividad.getApplication()).dbMapRepository.insertMap(newMap);
        return mapId;
    }

    public List<Map> getAllMaps(){
        return ((Aplication) actividad.getApplication()).dbMapRepository.getAllMaps();
    }

    public List<Map> getMapsByName(String name){
        return ((Aplication) actividad.getApplication()).dbMapRepository.getMapsByName(name);
    }

    public Map getMapById(long idMap){
        return ((Aplication) actividad.getApplication()).dbMapRepository.getMapById(idMap);
    }
}
