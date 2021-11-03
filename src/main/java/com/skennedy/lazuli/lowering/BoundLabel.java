package com.skennedy.lazuli.lowering;

public class BoundLabel {

    private final String name;

    public BoundLabel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
