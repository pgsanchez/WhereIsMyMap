package com.pgsanchez.whereismymap.domain;

public enum DistanceType {
    SIN_ESPECIFICAR, MEDIA, LARGA, SPRINT;

    public static String[] getDistances() {
        String[] distances = new String[DistanceType.values().length];
        for (DistanceType distancia : DistanceType.values()) {
            distances[distancia.ordinal()] = distancia.name();
        }
        return distances;
    }
}
