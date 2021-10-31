package com.skennedy.lazuli.typebinding;

public class VariableSymbol extends Symbol {

    private final TypeSymbol type;
    private final BoundExpression range;
    private final boolean readOnly;

    public VariableSymbol(String name, TypeSymbol type, BoundExpression range, boolean readOnly) {
        super(name);
        this.type = type;
        this.range = range;
        this.readOnly = readOnly;
    }

    @Override
    public SymbolType getSymbolType() {
        return SymbolType.VARIABLE;
    }

    public TypeSymbol getType() {
        return type;
    }

    public BoundExpression getRange() {
        return range;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
