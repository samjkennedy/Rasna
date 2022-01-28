package com.skennedy.rasna.typebinding;


import java.util.List;
import java.util.stream.Collectors;

public class FunctionSymbol extends Symbol {
    private final TypeSymbol type;
    private final List<BoundFunctionParameterExpression> arguments;
    private final BoundExpression guard;

    public FunctionSymbol(String name, TypeSymbol type, List<BoundFunctionParameterExpression> arguments, BoundExpression guard) {
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

    public List<BoundFunctionParameterExpression> getArguments() {
        return arguments;
    }

    public BoundExpression getGuard() {
        return guard;
    }

    public String getSignature() {
        return getName() + "(" + arguments.stream()
                .map(BoundFunctionParameterExpression::getSignature)
                .collect(Collectors.joining(", "))
                + "): " + type;
    }

    @Override
    public String toString() {
        return getSignature();
    }
}
