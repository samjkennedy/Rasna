package com.skennedy.lazuli.typebinding;

import java.util.Collections;
import java.util.Map;

import static com.skennedy.lazuli.typebinding.SymbolType.VARIABLE;

public class ArrayTypeSymbol extends TypeSymbol {

    private final TypeSymbol type;

    public ArrayTypeSymbol(TypeSymbol type) {
        super(type.getName(),
                Collections.emptyMap(),    //TODO: split, reduce, etc etc
                intrinsicMemberVariables
        );
        this.type = type;
    }

    private static final VariableSymbol arrayLen = new VariableSymbol("size", TypeSymbol.INT, null, false);

    private static final Map<String, VariableSymbol> intrinsicMemberVariables = Map.of(
            "size", arrayLen
    );

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
