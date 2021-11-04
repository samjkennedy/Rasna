package com.skennedy.lazuli.typebinding;

public class TypeSymbol extends Symbol {


    public static final TypeSymbol VOID = new TypeSymbol("Void");
    public static final TypeSymbol TYPE = new TypeSymbol("Type");
    public static final TypeSymbol INT = new TypeSymbol("Int");
    public static final TypeSymbol REAL = new TypeSymbol("Real");
    public static final TypeSymbol BOOL = new TypeSymbol("Bool");
    public static final TypeSymbol FUNCTION = new TypeSymbol("Function");
    public static final TypeSymbol INT_ARRAY = new TypeSymbol("IntArray"); //TODO: TEMP

    public TypeSymbol(String name) {
        super(name);
    }

    @Override
    public SymbolType getSymbolType() {
        return SymbolType.TYPE;
    }

    public boolean isAssignableFrom(TypeSymbol other) {

        if (this.getName().equals(other.getName())) {
            return true;
        }
        if (this == REAL) {
            return other == INT;
        }
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }
}
