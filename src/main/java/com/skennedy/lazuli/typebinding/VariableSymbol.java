package com.skennedy.lazuli.typebinding;

public class VariableSymbol extends Symbol {

    private final TypeSymbol type;
    private final BoundExpression guard;
    private final boolean readOnly;

    public VariableSymbol(String name, TypeSymbol type, BoundExpression guard, boolean readOnly) {
        super(name);
        this.type = type;
        this.guard = guard;
        this.readOnly = readOnly;
    }

    @Override
    public SymbolType getSymbolType() {
        return SymbolType.VARIABLE;
    }

    public TypeSymbol getType() {
        return type;
    }

    public BoundExpression getGuard() {
        return guard;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
