package com.pgsanchez.whereismymap.repository;

import com.pgsanchez.whereismymap.domain.Map;

import java.util.List;

public interface MapRepository {
    // En esta interface se definirán los métodos necesarios para
    // acceder a la BD, a la tabla MAPAS
    // Métodos:

    List<Map> getAllMaps();
    Map getMapById(long id);
    List<Map> getMapsByName(String name);

    long insertMap(Map mapa);
    int updateMap(Map mapa);
    int deleteMap(Map mapa);
}
