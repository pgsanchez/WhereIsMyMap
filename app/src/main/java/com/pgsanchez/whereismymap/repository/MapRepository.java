package com.pgsanchez.whereismymap.repository;

import com.pgsanchez.whereismymap.domain.Map;

import java.util.List;

public interface MapRepository {
    // En esta interface se definirán los métodos necesarios para
    // acceder a la BD, a la tabla MAPAS
    // Métodos:

    List<Map> getAllMaps();
    Map getMapById(int id);
    List<Map> getMapsByName(String name);

    int insertMap(Map mapa);
    void updateMap(Map mapa);
    void deleteMap(Map mapa);
    void deleteMap(int id);
}
