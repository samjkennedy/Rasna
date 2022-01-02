package com.skennedy.lazuli.typebinding;

import java.util.Collections;

import static com.skennedy.lazuli.typebinding.SymbolType.VARIABLE;

public class ArrayTypeSymbol extends TypeSymbol {

    private final TypeSymbol type;

    public ArrayTypeSymbol(TypeSymbol type) {
        super(type.getName(),
                Collections.emptyMap(),    //TODO: split, reduce, etc etc
                Collections.emptyMap()     //TODO: len, etc
        );
        this.type = type;
    }

    public TypeSymbol getType() {
        return type;
    }

    @Override
    public SymbolType getSymbolType() {
        return VARIABLE;
    }

    @Override
    public String toString() {
        return getName() + "[]";
    }
}
