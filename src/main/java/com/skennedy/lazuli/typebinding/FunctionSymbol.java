package com.skennedy.lazuli.typebinding;


import java.util.List;

public class FunctionSymbol extends Symbol {
    private final TypeSymbol type;
    private final List<BoundFunctionArgumentExpression> arguments;
    private final BoundExpression range;

    public FunctionSymbol(String name, TypeSymbol type, List<BoundFunctionArgumentExpression> arguments, BoundExpression range) {
        super(name);
        this.type = type;
        this.arguments = arguments;
        this.range = range;
    }

    @Override
    public SymbolType getSymbolType() {
        return SymbolType.FUNCTION;
    }

    public TypeSymbol getType() {
        return type;
    }

    public List<BoundFunctionArgumentExpression> getArguments() {
        return arguments;
    }

    public BoundExpression getRange() {
        return range;
    }


}
