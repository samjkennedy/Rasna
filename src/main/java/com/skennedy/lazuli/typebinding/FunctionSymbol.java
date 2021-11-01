package com.skennedy.lazuli.typebinding;


public class FunctionSymbol extends Symbol {
    private final TypeSymbol type;
    private final BoundExpression range;

    public FunctionSymbol(String name, TypeSymbol type, BoundExpression range) {
        super(name);
        this.type = type;
        this.range = range;
    }

    @Override
    public SymbolType getSymbolType() {
        return SymbolType.FUNCTION;
    }

    public TypeSymbol getType() {
        return type;
    }

    public BoundExpression getRange() {
        return range;
    }


}
