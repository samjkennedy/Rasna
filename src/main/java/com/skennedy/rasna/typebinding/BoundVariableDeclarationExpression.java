package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundVariableDeclarationExpression implements BoundExpression {

    private final VariableSymbol variable;
    private final BoundExpression guard;
    private final BoundExpression initialiser;
    private final boolean readOnly;

    public BoundVariableDeclarationExpression(VariableSymbol variable, BoundExpression guard, BoundExpression initialiser, boolean readOnly) {
        this.variable = variable;
        this.guard = guard;
        this.initialiser = initialiser;
        this.readOnly = readOnly;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.VARIABLE_DECLARATION;
    }

    @Override
    public TypeSymbol getType() {
        return variable.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(guard, initialiser).iterator();
    }

    public VariableSymbol getVariable() {
        return variable;
    }

    public BoundExpression getGuard() {
        return guard;
    }

    public BoundExpression getInitialiser() {
        return initialiser;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
