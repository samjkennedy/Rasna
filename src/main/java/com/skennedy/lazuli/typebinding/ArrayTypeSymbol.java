package com.skennedy.lazuli.typebinding;

import static com.skennedy.lazuli.typebinding.SymbolType.VARIABLE;

public class ArrayTypeSymbol extends TypeSymbol {

    private final TypeSymbol type;

    public ArrayTypeSymbol(TypeSymbol type) {
        super(type.getName());
        this.type = type;
    }

    public TypeSymbol getType() {
        return type;
    }

    @Override
    public SymbolType getSymbolType() {
        return VARIABLE;
    }
}
