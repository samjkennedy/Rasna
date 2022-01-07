package com.skennedy.rasna.typebinding;


import java.util.List;

public class FunctionSymbol extends Symbol {
    private final TypeSymbol type;
    private final List<BoundFunctionArgumentExpression> arguments;
    private final BoundExpression guard;

    public FunctionSymbol(String name, TypeSymbol type, List<BoundFunctionArgumentExpression> arguments, BoundExpression guard) {
        super(name);
        this.type = type;
        this.arguments = arguments;
        this.guard = guard;
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

    public BoundExpression getGuard() {
        return guard;
    }


}
