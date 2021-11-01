package com.skennedy.lazuli.typebinding;

import java.util.Objects;

public abstract class Symbol {

    private final String name;

    public Symbol(String name) {
        this.name = name;
    }

    public abstract SymbolType getSymbolType();

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Symbol)) return false;
        Symbol symbol = (Symbol) o;
        return Objects.equals(getName(), symbol.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
