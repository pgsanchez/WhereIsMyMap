package com.pgsanchez.whereismymap.domain;

public enum CategoryName {
    M40, F40, M35, F35;

    public static String[] getCategories() {
        String[] categories = new String[CategoryName.values().length];
        for (CategoryName category : CategoryName.values()) {
            categories[category.ordinal()] = category.name();
        }
        return categories;
    }
}
