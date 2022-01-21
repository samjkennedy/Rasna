package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TypeSymbol extends Symbol {

    public static final TypeSymbol VOID = new TypeSymbol("Void", new LinkedHashMap<>());
    public static final TypeSymbol BOOL = new TypeSymbol("Bool", new LinkedHashMap<>());
    public static final TypeSymbol CHAR = new TypeSymbol("Char", new LinkedHashMap<>());
    public static final TypeSymbol INT = new TypeSymbol("Int", new LinkedHashMap<>());
    public static final TypeSymbol REAL = new TypeSymbol("Real", new LinkedHashMap<>());
    public static final TypeSymbol STRING = new TypeSymbol("String", new LinkedHashMap<>());
    public static final TypeSymbol TYPE = new TypeSymbol("Type", new LinkedHashMap<>());
    public static final TypeSymbol ANY = new TypeSymbol("Any", new LinkedHashMap<>());
    public static final TypeSymbol ERROR = new TypeSymbol("Error", new LinkedHashMap<>());
    public static final TypeSymbol FUNCTION = new TypeSymbol("Function", new LinkedHashMap<>()); //TODO: Should be similar to ArrayTypeSymbol

    private final LinkedHashMap<String, VariableSymbol> fields;

    public TypeSymbol(String name, LinkedHashMap<String, VariableSymbol> fields) {
        super(name);
        this.fields = fields;
    }

    public static List<TypeSymbol> getPrimitives() {
        return Arrays.asList(BOOL, CHAR, INT, REAL, STRING, TYPE, ANY);
    }

    public Map<String, VariableSymbol> getFields() {
        return fields;
    }

    @Override
    public SymbolType getSymbolType() {
        return SymbolType.TYPE;
    }

    public boolean isAssignableFrom(TypeSymbol other) {

        if (this.toString().equals(other.toString())) {
            return true;
        }
        //TODO: More sophisticated casting -> all INTs are REALs
        return this == ANY;
    }

    @Override
    public String toString() {
        return getName();
    }
}
