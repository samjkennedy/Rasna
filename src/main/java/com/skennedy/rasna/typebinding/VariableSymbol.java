package com.skennedy.rasna.typebinding;

import com.skennedy.rasna.parsing.Expression;

public class VariableSymbol extends Symbol {

    private final TypeSymbol type;
    private final BoundExpression guard;
    private final boolean readOnly;
    private final Expression declaration;

    public VariableSymbol(String name, TypeSymbol type, BoundExpression guard, boolean readOnly, Expression declaration) {
        super(name);
        this.type = type;
        this.guard = guard;
        this.readOnly = readOnly;
        this.declaration = declaration;
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

    public Expression getDeclaration() {
        return declaration;
    }

    @Override
    public String toString() {
        return getName();
    }
}
