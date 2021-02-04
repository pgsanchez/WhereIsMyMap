package com.pgsanchez.whereismymap.presentation;

import android.app.Application;

import com.pgsanchez.whereismymap.domain.Map;

import java.util.ArrayList;
import java.util.List;

public class Aplication extends Application {
    List mapList;
    @Override
    public void onCreate() {
        super.onCreate();
        mapList = new ArrayList<Map>();
    }
}
