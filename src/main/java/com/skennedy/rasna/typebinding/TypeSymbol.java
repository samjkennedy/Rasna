package com.skennedy.rasna.typebinding;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TypeSymbol extends Symbol {

    //TODO: Built in type functions
    public static final TypeSymbol VOID = new TypeSymbol("Void", Collections.emptyMap(), new LinkedHashMap<>());
    public static final TypeSymbol BOOL = new TypeSymbol("Bool", Collections.emptyMap(), new LinkedHashMap<>());
    public static final TypeSymbol INT = new TypeSymbol("Int", Collections.emptyMap(), new LinkedHashMap<>());
    public static final TypeSymbol REAL = new TypeSymbol("Real", Collections.emptyMap(), new LinkedHashMap<>());
    public static final TypeSymbol STRING = new TypeSymbol("String", Collections.emptyMap(), new LinkedHashMap<>());
    public static final TypeSymbol TYPE = new TypeSymbol("Type", Collections.emptyMap(), new LinkedHashMap<>());
    public static final TypeSymbol TUPLE = new TypeSymbol("Tuple", Collections.emptyMap(), new LinkedHashMap<>());
    public static final TypeSymbol ANY = new TypeSymbol("Any", Collections.emptyMap(), new LinkedHashMap<>());
    public static final TypeSymbol FUNCTION = new TypeSymbol("Function", Collections.emptyMap(), new LinkedHashMap<>()); //TODO: Should be similar to ArrayTypeSymbol

    private final Map<String, FunctionSymbol> functions;
    private final LinkedHashMap<String, VariableSymbol> fields;

    public TypeSymbol(String name, Map<String, FunctionSymbol> functions, LinkedHashMap<String, VariableSymbol> fields) {
        super(name);
        this.functions = functions;
        this.fields = fields;
    }

    public Map<String, FunctionSymbol> getFunctions() {
        return functions;
    }

    public Map<String, VariableSymbol> getFields() {
        return fields;
    }

    @Override
    public SymbolType getSymbolType() {
        return SymbolType.TYPE;
    }

    public boolean isAssignableFrom(TypeSymbol other) {

        if (this.getName().equals(other.getName())) {
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
