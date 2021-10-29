package com.skennedy.bixbite.typebinding;

public abstract class Symbol {

    private final String name;

    public Symbol(String name) {
        this.name = name;
    }

    public abstract SymbolType getSymbolType();

    public String getName() {
        return name;
    }
}
