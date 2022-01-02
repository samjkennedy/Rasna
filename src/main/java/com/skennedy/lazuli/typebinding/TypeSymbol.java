package com.skennedy.lazuli.typebinding;

import java.util.Collections;
import java.util.List;

public class TypeSymbol extends Symbol {

    //TODO: Built in type functions
    public static final TypeSymbol VOID = new TypeSymbol("Void", Collections.emptyList(), Collections.emptyList());
    public static final TypeSymbol BOOL = new TypeSymbol("Bool", Collections.emptyList(), Collections.emptyList());
    public static final TypeSymbol INT = new TypeSymbol("Int", Collections.emptyList(), Collections.emptyList());
    public static final TypeSymbol REAL = new TypeSymbol("Real", Collections.emptyList(), Collections.emptyList());
    public static final TypeSymbol STRING = new TypeSymbol("String", Collections.emptyList(), Collections.emptyList());
    public static final TypeSymbol TYPE = new TypeSymbol("Type", Collections.emptyList(), Collections.emptyList());
    public static final TypeSymbol TUPLE = new TypeSymbol("Tuple", Collections.emptyList(), Collections.emptyList());
    public static final TypeSymbol VAR = new TypeSymbol("Var", Collections.emptyList(), Collections.emptyList());
    public static final TypeSymbol FUNCTION = new TypeSymbol("Function", Collections.emptyList(), Collections.emptyList()); //TODO: Should be similar to ArrayTypeSymbol

    private final List<FunctionSymbol> functions;
    private final List<VariableSymbol> fields;

    public TypeSymbol(String name, List<FunctionSymbol> functions, List<VariableSymbol> fields) {
        super(name);
        this.functions = functions;
        this.fields = fields;
    }

    public List<FunctionSymbol> getFunctions() {
        return functions;
    }

    public List<VariableSymbol> getFields() {
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
        return this == VAR;
    }

    @Override
    public String toString() {
        return getName();
    }
}
