package com.skennedy.lazuli.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundVariableExpression implements BoundExpression {

    private final VariableSymbol variable;

    public BoundVariableExpression(VariableSymbol variable) {

        this.variable = variable;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.VARIABLE_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return variable.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }

    public VariableSymbol getVariable() {
        return variable;
    }
}
