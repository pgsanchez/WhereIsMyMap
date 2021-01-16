package com.pgsanchez.whereismymap.domain;

import java.util.Date;

public class Map {
    int id;             // Id del mapa. Se asginará automáticamente. No puede ser nulo, obviamente
    String name;        // Nombre del mapa. Tampoco puede ser nulo. Las búsquedas se harán por este nombre
    double latitude;
    double longitude;   // Coordenadas para geo-localizar el mapa. Puede ser 0, si no se sabe las coordenadas (que habrá alguno antiguo que no las sepa)
    String imgFileName; // Nombre del fichero de imagen del mapa. Tampoco puede ser nulo. Hay que cambiarle el nombre para que no se pueda repetir. Quizá como nombre del fichero se podría
                        // poner el nombre del mapa + el año: ElPardo2020
    // FileImage imgFile; // Imagen en pequeño del mapa. La imagen grande se guardará en GoogleDrive

    String category;   // Categoría de la carrera. M40. Puede ser nulo
    String distance;   // Distancia de la carrera. (media, larga, sprint) Puede ser nulo
    Date mapDate;       // puede ser nulo. Pero hay que gestionarlo bien en la BD
    Date raceDate;      // puede ser nulo. Pero hay que gestionarlo bien en la BD

    public Map() {
        // Valores por defecto
        id = 0;
        name = "noname";
        latitude = 0;
        longitude = 0;
        imgFileName = "";
        category = "";
        distance = "";
        mapDate = null;
        raceDate = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getImgFileName() {
        return imgFileName;
    }

    public void setImgFileName(String imgFileName) {
        this.imgFileName = imgFileName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public Date getMapDate() {
        return mapDate;
    }

    public void setMapDate(Date mapDate) {
        this.mapDate = mapDate;
    }

    public Date getRaceDate() {
        return raceDate;
    }

    public void setRaceDate(Date raceDate) {
        this.raceDate = raceDate;
    }
}
