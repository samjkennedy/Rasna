package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TypeSymbol extends Symbol {

    public static final TypeSymbol UNIT = new TypeSymbol("Unit", new LinkedHashMap<>());
    public static final TypeSymbol BOOL = new TypeSymbol("Bool", new LinkedHashMap<>());
    public static final TypeSymbol CHAR = new TypeSymbol("Char", new LinkedHashMap<>());
    public static final TypeSymbol INT = new TypeSymbol("Int", new LinkedHashMap<>());
    public static final TypeSymbol REAL = new TypeSymbol("Real", new LinkedHashMap<>());
    public static final TypeSymbol STRING = new TypeSymbol("String", new LinkedHashMap<>(Map.of(
            "len", new VariableSymbol("len", INT, null, true, null)
    )));
    public static final TypeSymbol TYPE = new TypeSymbol("Type", new LinkedHashMap<>());
    public static final TypeSymbol ANY = new TypeSymbol("Any", new LinkedHashMap<>());
    public static final TypeSymbol ERROR = new TypeSymbol("Error", new LinkedHashMap<>());
    public static final TypeSymbol FUNCTION = new TypeSymbol("Function", new LinkedHashMap<>());
    public static final TypeSymbol FILE = new TypeSymbol("File", new LinkedHashMap<>());

    private final LinkedHashMap<String, VariableSymbol> fields;

    // Read as right can implicitly be casted to left
    private static final Map<TypeSymbol, TypeSymbol> implicitCasts = Map.of(
            REAL, INT
    );

    public TypeSymbol(String name, LinkedHashMap<String, VariableSymbol> fields) {
        super(name);
        this.fields = fields;
    }

    public static List<TypeSymbol> getPrimitives() {
        return Arrays.asList(UNIT, BOOL, CHAR, INT, REAL, STRING, TYPE, ANY, FILE);
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
        return implicitCasts.get(this) == other;
    }



    @Override
    public String toString() {
        return getName();
    }
}
