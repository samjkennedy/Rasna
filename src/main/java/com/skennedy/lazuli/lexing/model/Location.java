package com.skennedy.lazuli.lexing.model;

public class Location {

    private final String filePath;
    private final int row;
    private final int column;

    public Location(String filePath, int row, int column) {
        this.filePath = filePath;
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return filePath + ":" + row + ":" + column;
    }
}
