package com.core.util;

/**
 * @author Taras Zubrei
 */
public enum Resource {
    UO;
    private final String name;

    Resource() {
        this.name = name().toLowerCase();
    }

    public String getName() {
        return name;
    }

    public static Resource fromName(String name) {
        return Resource.valueOf(name.toUpperCase());
    }
}
