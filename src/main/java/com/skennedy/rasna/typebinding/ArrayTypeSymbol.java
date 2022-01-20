package com.skennedy.rasna.typebinding;

import java.util.LinkedHashMap;
import java.util.Objects;

import static com.skennedy.rasna.typebinding.SymbolType.VARIABLE;

public class ArrayTypeSymbol extends TypeSymbol {

    private final TypeSymbol type;

    public ArrayTypeSymbol(TypeSymbol type) {
        super(type.getName(),
                //TODO: split, reduce, etc etc
                new LinkedHashMap<>()
        );
        this.type = type;
    }

    //private static final VariableSymbol arrayLen = new VariableSymbol("size", TypeSymbol.INT, null, false, declaration);

//    private static final TreeMap<String, VariableSymbol> intrinsicMemberVariables = TreeMap.of(
//            "size", arrayLen
//    );


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArrayTypeSymbol)) {
            return false;
        }
        ArrayTypeSymbol that = (ArrayTypeSymbol) o;
        return getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getType());
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
